package app.timetable_back.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "lessons_room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonRoom {

    @EmbeddedId
    private LessonRoomId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("lessonId")
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("roomId")
    @JoinColumn(name = "room_id")
    private Room room;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;
}