package app.timetable_back.dto;

import app.timetable_back.entity.RecurrenceRuleType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonDto {

    private Long id;

    @NotNull(message = "Start time is required")
    private LocalDateTime startAt;

    @NotNull(message = "End time is required")
    private LocalDateTime endAt;

    // NEW: список аудиторий вместо одной
    private List<Long> roomIds;

    @Positive(message = "Subject ID must be positive")
    private Long subjectId;

    @Positive(message = "Teacher ID must be positive")
    private Long teacherId;

    private RecurrenceRuleType ruleType;

    @Builder.Default
    private Boolean isOverride = false;

    @Builder.Default
    private Boolean isCancelled = false;

    private List<Long> groupIds;
}
