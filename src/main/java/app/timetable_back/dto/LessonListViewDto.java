package app.timetable_back.dto;

import app.timetable_back.entity.RecurrenceRuleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для отображения занятия в списке (без id, createdAt, updatedAt)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о занятии в списке")
public class LessonListViewDto {

    @Schema(description = "Время начала", example = "2026-03-15T10:00:00")
    private LocalDateTime startAt;

    @Schema(description = "Время окончания", example = "2026-03-15T11:30:00")
    private LocalDateTime endAt;

    @Schema(description = "Тип повторения", example = "WEEKLY")
    private RecurrenceRuleType ruleType;

    @Schema(description = "Аудитория")
    private RoomInfo room;

    @Schema(description = "Предмет")
    private SubjectInfo subject;

    @Schema(description = "Преподаватель")
    private TeacherInfo teacher;

    @Schema(description = "Переопределено", example = "false")
    private Boolean isOverride;

    @Schema(description = "Отменено", example = "false")
    private Boolean isCancelled;

    @Schema(description = "Список ID групп", example = "[1, 2]")
    private List<Long> groupIds;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Информация об аудитории")
    public static class RoomInfo {
        @Schema(description = "Номер аудитории", example = "228")
        private String roomNumber;

        @Schema(description = "Корпус", example = "Главный корпус")
        private String building;

        @Schema(description = "Вместимость", example = "52")
        private Integer capacity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Информация о предмете")
    public static class SubjectInfo {
        @Schema(description = "Название", example = "Высшая математика")
        private String name;

        @Schema(description = "Код", example = "MATH-101")
        private String code;

        @Schema(description = "Факультет", example = "Факультет математики")
        private String faculty;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Информация о преподавателе")
    public static class TeacherInfo {
        @Schema(description = "Имя", example = "Pyotr")
        private String firstName;

        @Schema(description = "Фамилия", example = "Petrov")
        private String lastName;

        @Schema(description = "Email", example = "p@gmail.com")
        private String email;
    }
}
