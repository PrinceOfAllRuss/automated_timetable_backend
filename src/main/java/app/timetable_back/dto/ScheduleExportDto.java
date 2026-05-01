package app.timetable_back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTO для экспорта расписания по диапазону дат
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleExportDto {

    /**
     * Расписание по дням
     * Ключ - дата
     * Значение - список уроков за этот день
     */
    private Map<LocalDate, List<LessonInfo>> scheduleByDate;

    /**
     * Список всех групп для колонок
     */
    private List<GroupInfo> groups;

    /**
     * Список всех временных слотов (уникальные пары время начала-окончания)
     */
    private List<TimeSlot> timeSlots;

    /**
     * Информация о группе
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupInfo {
        private Long id;
        private String name;
    }

    /**
     * Временной слот (пара)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlot {
        private String timeRange;
        private String sortKey;
    }

    /**
     * Информация об уроке для экспорта в Excel
     * Один урок для одной группы
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonInfo {
        private Long groupId;
        private String groupName;
        private String startTime;
        private String endTime;
        private String timeRange;
        private String subjectName;
        private String teacherName;
        private String roomNumber;
        private String building;
        private Boolean isCancelled;

        public String getTeacherDisplayName() {
            return teacherName != null ? teacherName : "";
        }

        public String getRoomDisplayName() {
            if (roomNumber == null || roomNumber.isEmpty()) {
                return "";
            }
            if (building == null || building.isEmpty()) {
                return "ауд. " + roomNumber;
            }
            return "ауд. " + roomNumber + " (" + building + ")";
        }
    }
}
