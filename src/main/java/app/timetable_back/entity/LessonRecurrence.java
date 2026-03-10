package app.timetable_back.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "lesson_recurrence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonRecurrence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "rule_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @NotNull
    private RecurrenceRuleType ruleType;

    @Column(name = "semester_start", nullable = false)
    @NotNull
    private LocalDate semesterStart;

    @Column(name = "semester_end", nullable = false)
    @NotNull
    private LocalDate semesterEnd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}
