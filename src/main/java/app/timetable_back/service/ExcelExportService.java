package app.timetable_back.service;

import app.timetable_back.dto.ScheduleExportDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Сервис для экспорта расписания в Excel
 */
@Service
public class ExcelExportService {

    private static final String DAY_NAME_FORMAT = "EEEE, dd.MM.yyyy";
    private static final Locale RUSSIAN_LOCALE = new Locale("ru", "RU");

    private static final short CANCELLED_BG_COLOR = IndexedColors.PINK.getIndex();
    
    // Приглушенные цвета для групп
    private static final IndexedColors[] GROUP_COLORS = {
        IndexedColors.LIGHT_BLUE,
        IndexedColors.LIGHT_GREEN,
        IndexedColors.LIGHT_YELLOW,
        IndexedColors.LAVENDER,
        IndexedColors.ROSE,
        IndexedColors.SKY_BLUE,
        IndexedColors.SEA_GREEN,
        IndexedColors.CORAL,
        IndexedColors.PLUM,
        IndexedColors.TAN
    };

    /**
     * Генерирует Excel файл с расписанием по диапазону дат
     */
    public byte[] generateExcel(ScheduleExportDto scheduleDto) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Map<LocalDate, List<ScheduleExportDto.LessonInfo>> scheduleByDate = scheduleDto.getScheduleByDate();
            List<LocalDate> sortedDates = new ArrayList<>(scheduleByDate.keySet());
            sortedDates.sort(LocalDate::compareTo);

            for (LocalDate date : sortedDates) {
                List<ScheduleExportDto.LessonInfo> lessons = scheduleByDate.get(date);
                createSheet(workbook, date, lessons, scheduleDto.getGroups(), scheduleDto.getTimeSlots());
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Создает лист для одного дня
     */
    private void createSheet(Workbook workbook, LocalDate date, List<ScheduleExportDto.LessonInfo> lessons,
                              List<ScheduleExportDto.GroupInfo> groups,
                              List<ScheduleExportDto.TimeSlot> timeSlots) {

        String sheetName = date.format(DateTimeFormatter.ofPattern("dd.MM", RUSSIAN_LOCALE));
        if (sheetName.length() > 31) {
            sheetName = sheetName.substring(0, 31);
        }
        Sheet sheet = workbook.createSheet(sheetName);

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle timeSlotStyle = createTimeSlotStyle(workbook);
        CellStyle lessonStyle = createLessonStyle(workbook);
        CellStyle cancelledStyle = createCancelledStyle(workbook);
        CellStyle largeFontStyle = createLargeFontStyle(workbook);
        
        // Создаем стили для групп с цветами
        Map<Long, CellStyle> groupStyles = new HashMap<>();
        for (int i = 0; i < groups.size(); i++) {
            CellStyle groupStyle = createGroupStyle(workbook, i);
            groupStyles.put(groups.get(i).getId(), groupStyle);
        }

        // Строка 0: заголовок с датой
        Row headerRow = sheet.createRow(0);
        Cell headerCell = headerRow.createCell(0);
        String dateTitle = capitalizeFirst(date.format(DateTimeFormatter.ofPattern(DAY_NAME_FORMAT, RUSSIAN_LOCALE)));
        headerCell.setCellValue(dateTitle);
        headerCell.setCellStyle(largeFontStyle);

        int totalColumns = groups.size() + 1;
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, totalColumns - 1));

        // Строка 1: заголовки колонок
        Row groupHeaderRow = sheet.createRow(1);

        Cell timeHeaderCell = groupHeaderRow.createCell(0);
        timeHeaderCell.setCellValue("Время");
        timeHeaderCell.setCellStyle(headerStyle);

        for (int i = 0; i < groups.size(); i++) {
            Cell groupCell = groupHeaderRow.createCell(i + 1);
            groupCell.setCellValue(groups.get(i).getName());
            groupCell.setCellStyle(groupStyles.get(groups.get(i).getId()));
        }

        // Строки с уроками
        int rowNum = 2;

        for (ScheduleExportDto.TimeSlot timeSlot : timeSlots) {
            Row row = sheet.createRow(rowNum++);

            Cell timeCell = row.createCell(0);
            timeCell.setCellValue(timeSlot.getTimeRange());
            timeCell.setCellStyle(timeSlotStyle);

            for (int i = 0; i < groups.size(); i++) {
                Cell lessonCell = row.createCell(i + 1);
                Long groupId = groups.get(i).getId();

                ScheduleExportDto.LessonInfo lesson = findLessonForGroupAndTime(lessons, groupId, timeSlot.getTimeRange());

                if (lesson != null) {
                    lessonCell.setCellValue(formatLessonCell(lesson));
                    // Белый фон для ячеек уроков, розовый для отменённых
                    lessonCell.setCellStyle(lesson.getIsCancelled() != null && lesson.getIsCancelled()
                            ? cancelledStyle
                            : lessonStyle);
                }
            }
        }

        for (int i = 0; i < totalColumns; i++) {
            sheet.autoSizeColumn(i);
        }

        sheet.setColumnWidth(0, 20 * 256);
    }

    private ScheduleExportDto.LessonInfo findLessonForGroupAndTime(
            List<ScheduleExportDto.LessonInfo> lessons, Long groupId, String timeRange) {
        for (ScheduleExportDto.LessonInfo lesson : lessons) {
            if (lesson.getGroupId().equals(groupId) && lesson.getTimeRange().equals(timeRange)) {
                return lesson;
            }
        }
        return null;
    }

    private String formatLessonCell(ScheduleExportDto.LessonInfo lesson) {
        StringBuilder sb = new StringBuilder();

        if (lesson.getSubjectName() != null && !lesson.getSubjectName().isEmpty()) {
            sb.append(lesson.getSubjectName());
        }

        if (lesson.getTeacherName() != null && !lesson.getTeacherName().isEmpty()) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(lesson.getTeacherDisplayName());
        }

        if (lesson.getRoomNumber() != null && !lesson.getRoomNumber().isEmpty()) {
            if (sb.length() > 0) sb.append("\n");
            sb.append(lesson.getRoomDisplayName());
        }

        return sb.toString();
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createTimeSlotStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createGroupStyle(Workbook workbook, int groupIndex) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        
        // Выбираем цвет для группы по индексу (циклически)
        IndexedColors color = GROUP_COLORS[groupIndex % GROUP_COLORS.length];
        style.setFillForegroundColor(color.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createLessonStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createCancelledStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setItalic(true);
        style.setFont(font);
        style.setFillForegroundColor(CANCELLED_BG_COLOR);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createLargeFontStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
