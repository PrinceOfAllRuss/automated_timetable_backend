package app.timetable_back.dto;

import app.timetable_back.entity.RecurrenceRuleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;  // ← Вернули List

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Информация о занятии")
public class LessonResponseDto {

    @Schema(description = "ID занятия", example = "1")
    private Long id;

    @Schema(description = "Время начала", example = "2026-03-15T10:00:00")
    private LocalDateTime startAt;

    @Schema(description = "Время окончания", example = "2026-03-15T11:30:00")
    private LocalDateTime endAt;

    @Schema(description = "Тип повторения", example = "WEEKLY")
    private RecurrenceRuleType ruleType;

    @Schema(description = "Список аудиторий")
    private List<RoomInfo> rooms;  // ← List вместо Set

    @Schema(description = "Предмет")
    private SubjectInfo subject;

    @Schema(description = "Преподаватель")
    private TeacherInfo teacher;

    @Schema(description = "Переопределено", example = "false")
    private Boolean isOverride;

    @Schema(description = "Отменено", example = "false")
    private Boolean isCancelled;

    @Schema(description = "Список ID групп", example = "[1, 2]")
    private List<Long> groupIds;  // ← List вместо Set

    @Schema(description = "Время создания", example = "2026-03-14T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Время обновления", example = "2026-03-14T12:00:00")
    private LocalDateTime updatedAt;

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @Schema(description = "Информация об аудитории")
    public static class RoomInfo {
        @Schema(description = "ID аудитории", example = "1")
        private Long id;
        @Schema(description = "Номер аудитории", example = "228")
        private String roomNumber;
        @Schema(description = "Корпус", example = "Главный корпус")
        private String building;
        @Schema(description = "Вместимость", example = "52")
        private Integer capacity;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @Schema(description = "Информация о предмете")
    public static class SubjectInfo {
        @Schema(description = "ID предмета", example = "1")
        private Long id;
        @Schema(description = "Название", example = "Высшая математика")
        private String name;
        @Schema(description = "Код", example = "MATH-101")
        private String code;
        @Schema(description = "Факультет", example = "Факультет математики")
        private String faculty;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    @Schema(description = "Информация о преподавателе")
    public static class TeacherInfo {
        @Schema(description = "ID преподавателя", example = "2")
        private Long id;
        @Schema(description = "Имя", example = "Pyotr")
        private String firstName;
        @Schema(description = "Фамилия", example = "Petrov")
        private String lastName;
        @Schema(description = "Email", example = "p@gmail.com")
        private String email;
    }
}