package app.timetable_back.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTO для экспорта расписания по диапазону дат (формат по аудиториям)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleExportDto {

    private Map<LocalDate, List<LessonInfo>> scheduleByDate;
    private List<RoomInfo> rooms;
    private List<TimeSlot> timeSlots;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoomInfo {
        private Long id;
        private String roomNumber;
        private String building;
        private Integer capacity;
        
        public String getDisplayName() {
            return roomNumber + " (" + capacity + " мест)";
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlot {
        private String timeRange;
        private String sortKey;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LessonInfo {
        private Long roomId;
        private String roomName;
        private String startTime;
        private String endTime;
        private String timeRange;
        private String subjectName;
        private String teacherName;
        private String groupsList;
        private String building;
        private Boolean isCancelled;
        private String colorKey;
        private List<Long> mergedRoomIds;

        public String getCellContent() {
            if (subjectName == null || subjectName.isEmpty()) return "Available";
            StringBuilder sb = new StringBuilder(subjectName);
            if (groupsList != null && !groupsList.isEmpty()) {
                sb.append(", Группы: ").append(groupsList);
            }
            if (teacherName != null && !teacherName.isEmpty()) {
                sb.append(", ").append(teacherName);
            }
            return sb.toString();
        }
    }
}