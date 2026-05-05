package app.timetable_back.service;

import app.timetable_back.dto.ScheduleExportDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ExcelExportService {

    private static final String DAY_NAME_FORMAT = "EEEE, dd.MM.yyyy";
    // Java 25: современный способ создания Locale
    private static final Locale RUSSIAN_LOCALE = Locale.of("ru", "RU");

    public byte[] generateExcel(ScheduleExportDto scheduleDto) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            List<LocalDate> sortedDates = new ArrayList<>(scheduleDto.getScheduleByDate().keySet());
            sortedDates.sort(LocalDate::compareTo);

            for (LocalDate date : sortedDates) {
                List<ScheduleExportDto.LessonInfo> lessons = scheduleDto.getScheduleByDate().get(date);
                createSheet((XSSFWorkbook) workbook, date, lessons, scheduleDto.getRooms(), scheduleDto.getTimeSlots());
            }
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void createSheet(XSSFWorkbook wb, LocalDate date, List<ScheduleExportDto.LessonInfo> lessons,
                             List<ScheduleExportDto.RoomInfo> rooms,
                             List<ScheduleExportDto.TimeSlot> timeSlots) {

        String sheetName = date.format(DateTimeFormatter.ofPattern("dd.MM", RUSSIAN_LOCALE));
        if (sheetName.length() > 31) sheetName = sheetName.substring(0, 31);
        Sheet sheet = wb.createSheet(sheetName);

        CellStyle headerStyle = createHeaderStyle(wb);
        CellStyle timeSlotStyle = createTimeSlotStyle(wb);
        CellStyle cancelledStyle = createCancelledStyle(wb);
        CellStyle largeFontStyle = createLargeFontStyle(wb);
        CellStyle availableStyle = createAvailableStyle(wb);
        
        Map<Long, CellStyle> roomHeaderStyles = new HashMap<>();
        for (int i = 0; i < rooms.size(); i++) {
            roomHeaderStyles.put(rooms.get(i).getId(), createRoomHeaderStyle(wb, i));
        }
        Map<String, CellStyle> lessonColorCache = new HashMap<>();

        // Заголовок даты
        Row headerRow = sheet.createRow(0);
        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue(capitalizeFirst(date.format(DateTimeFormatter.ofPattern(DAY_NAME_FORMAT, RUSSIAN_LOCALE))));
        headerCell.setCellStyle(largeFontStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, rooms.size()));

        // Шапка: аудитории
        Row roomHeaderRow = sheet.createRow(1);
        Cell timeHeader = roomHeaderRow.createCell(0);
        timeHeader.setCellValue("Время");
        timeHeader.setCellStyle(headerStyle);

        for (int i = 0; i < rooms.size(); i++) {
            Cell cell = roomHeaderRow.createCell(i + 1);
            ScheduleExportDto.RoomInfo room = rooms.get(i);
            cell.setCellValue(room.getDisplayName());
            cell.setCellStyle(roomHeaderStyles.get(room.getId()));
        }

        // Тело таблицы
        int rowNum = 2;
        Set<String> processedMerges = new HashSet<>();

        for (ScheduleExportDto.TimeSlot slot : timeSlots) {
            Row row = sheet.createRow(rowNum++);
            
            Cell timeCell = row.createCell(0);
            timeCell.setCellValue(slot.getTimeRange());
            timeCell.setCellStyle(timeSlotStyle);

            for (int colIdx = 0; colIdx < rooms.size(); colIdx++) {
                ScheduleExportDto.RoomInfo room = rooms.get(colIdx);
                Long roomId = room.getId();
                
                if (shouldSkipCell(lessons, roomId, slot.getTimeRange())) continue;

                Cell lessonCell = row.createCell(colIdx + 1);
                ScheduleExportDto.LessonInfo lesson = findLesson(lessons, roomId, slot.getTimeRange());

                if (lesson != null && lesson.getSubjectName() != null && !lesson.getSubjectName().isEmpty()) {
                    lessonCell.setCellValue(lesson.getCellContent());
                    
                    if (Boolean.TRUE.equals(lesson.getIsCancelled())) {
                        lessonCell.setCellStyle(cancelledStyle);
                    } else {
                        // Детерминированный цвет на основе content
                        CellStyle colorStyle = lessonColorCache.computeIfAbsent(
                                lesson.getCellContent(), 
                                key -> createLessonStyleWithColor(wb, lesson.getCellContent())
                        );
                        lessonCell.setCellStyle(colorStyle);
                    }
                    
                    // Горизонтальное слияние
                    if (lesson.getMergedRoomIds() != null && lesson.getMergedRoomIds().size() > 1) {
                        int firstCol = colIdx + 1;
                        int lastCol = firstCol + lesson.getMergedRoomIds().size() - 1;
                        String mergeKey = date + "_" + slot.getTimeRange() + "_" + roomId;
                        if (!processedMerges.contains(mergeKey)) {
                            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, firstCol, lastCol));
                            processedMerges.add(mergeKey);
                        }
                    }
                } else {
                    lessonCell.setCellValue("Available");
                    lessonCell.setCellStyle(availableStyle);
                }
            }
        }

        for (int i = 0; i <= rooms.size(); i++) sheet.autoSizeColumn(i);
        sheet.setColumnWidth(0, 20 * 256);
    }

    private boolean shouldSkipCell(List<ScheduleExportDto.LessonInfo> lessons, Long roomId, String timeRange) {
        ScheduleExportDto.LessonInfo lesson = findLesson(lessons, roomId, timeRange);
        return lesson != null && lesson.getMergedRoomIds() != null && 
               !lesson.getMergedRoomIds().isEmpty() && 
               !lesson.getMergedRoomIds().get(0).equals(roomId);
    }

    private ScheduleExportDto.LessonInfo findLesson(List<ScheduleExportDto.LessonInfo> lessons, Long roomId, String timeRange) {
        return lessons.stream()
                .filter(l -> Objects.equals(l.getRoomId(), roomId) && Objects.equals(l.getTimeRange(), timeRange))
                .findFirst().orElse(null);
    }

    // === Генерация пастельных RGB цветов ===
    private static int[] generatePastelColor(int seed) {
        // Алгоритм гарантирует светлые тона (210-255) для читаемости чёрного текста
        int r = 215 + (Math.abs(seed * 13) % 40);
        int g = 220 + (Math.abs(seed * 29) % 35);
        int b = 225 + (Math.abs(seed * 41) % 30);
        return new int[]{r, g, b};
    }

    // === Factory methods for styles ===
    private XSSFCellStyle createBaseStyle(XSSFWorkbook wb, boolean bold, short fontSize, int[] rgb, 
                                          HorizontalAlignment align, boolean wrap) {
        XSSFCellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        if (bold) font.setBold(true);
        font.setFontHeightInPoints(fontSize);
        style.setFont(font);
        
        if (rgb != null) {
            XSSFColor color = new XSSFColor(new byte[]{(byte)rgb[0], (byte)rgb[1], (byte)rgb[2]}, null);
            style.setFillForegroundColor(color);
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        
        applyBorders(style);
        style.setAlignment(align);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        if (wrap) style.setWrapText(true);
        return style;
    }

    private CellStyle createHeaderStyle(XSSFWorkbook wb) { 
        return createBaseStyle(wb, true, (short)11, generatePastelColor(100), HorizontalAlignment.CENTER, true); 
    }
    
    private CellStyle createTimeSlotStyle(XSSFWorkbook wb) { 
        return createBaseStyle(wb, true, (short)10, generatePastelColor(200), HorizontalAlignment.CENTER, false); 
    }
    
    private CellStyle createRoomHeaderStyle(XSSFWorkbook wb, int idx) {
        // Уникальный цвет для каждой аудитории
        int[] rgb = generatePastelColor(idx * 79 + 300);
        return createBaseStyle(wb, true, (short)11, rgb, HorizontalAlignment.CENTER, true);
    }
    
    private CellStyle createLessonStyleWithColor(XSSFWorkbook wb, String contentKey) {
        int[] rgb = generatePastelColor(contentKey.hashCode());
        XSSFCellStyle style = createBaseStyle(wb, false, (short)10, rgb, HorizontalAlignment.LEFT, true);
        return style;
    }
    
    private CellStyle createCancelledStyle(XSSFWorkbook wb) {
        // Розовый для отмен (RGB: 255, 200, 210)
        XSSFCellStyle style = createBaseStyle(wb, false, (short)10, new int[]{255, 200, 210}, HorizontalAlignment.LEFT, true);
        Font f = wb.createFont(); 
        f.setFontHeightInPoints((short)10); 
        f.setItalic(true); 
        style.setFont(f);
        return style;
    }
    
    private CellStyle createAvailableStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = createBaseStyle(wb, false, (short)9, new int[]{245, 245, 245}, HorizontalAlignment.CENTER, false);
        Font f = wb.createFont(); 
        f.setColor(IndexedColors.GREY_50_PERCENT.getIndex()); 
        style.setFont(f);
        return style;
    }
    
    private CellStyle createLargeFontStyle(XSSFWorkbook wb) {
        XSSFCellStyle style = wb.createCellStyle();
        Font f = wb.createFont(); 
        f.setBold(true); 
        f.setFontHeightInPoints((short)16); 
        style.setFont(f);
        style.setAlignment(HorizontalAlignment.CENTER); 
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
    
    private void applyBorders(CellStyle s) {
        s.setBorderBottom(BorderStyle.THIN); 
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN); 
        s.setBorderRight(BorderStyle.THIN);
    }
    
    private String capitalizeFirst(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}