package app.timetable_back.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class LessonRoomId implements Serializable {
    private Long lessonId;
    private Long roomId;
}