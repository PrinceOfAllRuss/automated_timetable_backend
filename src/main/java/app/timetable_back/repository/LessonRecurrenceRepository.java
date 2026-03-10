package app.timetable_back.repository;

import app.timetable_back.entity.LessonRecurrence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonRecurrenceRepository extends JpaRepository<LessonRecurrence, Long> {
}
