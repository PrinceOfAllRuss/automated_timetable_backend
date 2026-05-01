package app.timetable_back.service;

import app.timetable_back.dto.ScheduleExportDto;
import app.timetable_back.entity.Lesson;
import app.timetable_back.entity.LessonStudentGroup;
import app.timetable_back.entity.StudentGroup;
import app.timetable_back.repository.GroupRepository;
import app.timetable_back.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для подготовки данных расписания для экспорта
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleExportService {

    private final LessonRepository lessonRepository;
    private final GroupRepository groupRepository;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Подготавливает данные расписания для экспорта по диапазону дат
     */
    public ScheduleExportDto getScheduleForDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        List<Lesson> lessons = lessonRepository.findByDateRange(startDateTime, endDateTime);

        // Получаем все группы, участвующие в уроках
        Set<StudentGroup> groupsSet = new LinkedHashSet<>();
        for (Lesson lesson : lessons) {
            if (lesson.getLessonStudentGroups() != null) {
                for (LessonStudentGroup lsg : lesson.getLessonStudentGroups()) {
                    groupsSet.add(lsg.getGroup());
                }
            }
        }

        List<StudentGroup> sortedGroups = new ArrayList<>(groupsSet);
        sortedGroups.sort(Comparator.comparing(StudentGroup::getName));

        List<ScheduleExportDto.GroupInfo> groupInfos = sortedGroups.stream()
                .map(g -> ScheduleExportDto.GroupInfo.builder()
                        .id(g.getId())
                        .name(g.getName())
                        .build())
                .collect(Collectors.toList());

        // Преобразуем уроки в LessonInfo
        List<ScheduleExportDto.LessonInfo> lessonInfos = new ArrayList<>();
        for (Lesson lesson : lessons) {
            if (lesson.getLessonStudentGroups() != null && !lesson.getLessonStudentGroups().isEmpty()) {
                for (LessonStudentGroup lsg : lesson.getLessonStudentGroups()) {
                    lessonInfos.add(toLessonInfo(lesson, lsg.getGroup()));
                }
            }
        }

        // Группируем по датам
        Map<LocalDate, List<ScheduleExportDto.LessonInfo>> scheduleByDate = lessonInfos.stream()
                .collect(Collectors.groupingBy(
                        dto -> LocalDate.parse(dto.getStartTime().substring(0, 10)),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // Получаем уникальные временные слоты (только время, без даты)
        List<ScheduleExportDto.TimeSlot> timeSlots = lessonInfos.stream()
                .map(dto -> ScheduleExportDto.TimeSlot.builder()
                        .timeRange(dto.getTimeRange())
                        .sortKey(dto.getStartTime())
                        .build())
                .distinct()
                .sorted(Comparator.comparing(ScheduleExportDto.TimeSlot::getSortKey))
                .collect(Collectors.toList());

        return ScheduleExportDto.builder()
                .scheduleByDate(scheduleByDate)
                .groups(groupInfos)
                .timeSlots(timeSlots)
                .build();
    }

    /**
     * Формирует строку времени только из времени (HH:mm-HH:mm)
     */
    private String formatTimeRangeOnly(Lesson lesson) {
        return lesson.getStartAt().format(TIME_FORMATTER) + "-" + lesson.getEndAt().format(TIME_FORMATTER);
    }

    /**
     * Преобразует Lesson entity в LessonInfo
     */
    private ScheduleExportDto.LessonInfo toLessonInfo(Lesson lesson, StudentGroup group) {
        String startTime = lesson.getStartAt().format(DATE_TIME_FORMATTER);
        String endTime = lesson.getEndAt().format(DATE_TIME_FORMATTER);
        String timeRange = formatTimeRangeOnly(lesson); // только HH:mm-HH:mm

        String teacherName = "";
        if (lesson.getTeacher() != null) {
            teacherName = formatTeacherName(
                    lesson.getTeacher().getFirstName(),
                    lesson.getTeacher().getLastName()
            );
        }

        return ScheduleExportDto.LessonInfo.builder()
                .groupId(group.getId())
                .groupName(group.getName())
                .startTime(startTime)
                .endTime(endTime)
                .timeRange(timeRange)
                .subjectName(lesson.getSubject() != null ? lesson.getSubject().getName() : "")
                .teacherName(teacherName)
                .roomNumber(lesson.getRoom() != null ? lesson.getRoom().getRoomNumber() : "")
                .building(lesson.getRoom() != null ? lesson.getRoom().getBuilding() : "")
                .isCancelled(lesson.getIsCancelled() != null ? lesson.getIsCancelled() : false)
                .build();
    }

    /**
     * Форматирует имя преподавателя как "Фамилия И.О."
     */
    private String formatTeacherName(String firstName, String lastName) {
        if (firstName == null || firstName.isEmpty()) {
            return lastName != null ? lastName : "";
        }
        if (lastName == null || lastName.isEmpty()) {
            return firstName;
        }

        String initials = "";
        String[] nameParts = firstName.split("\\s+");
        for (String part : nameParts) {
            if (!part.isEmpty()) {
                initials += Character.toUpperCase(part.charAt(0)) + ".";
            }
        }

        return lastName + " " + initials;
    }
}
