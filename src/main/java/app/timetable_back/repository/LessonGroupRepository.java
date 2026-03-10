package app.timetable_back.repository;

import app.timetable_back.entity.LessonGroup;
import app.timetable_back.entity.LessonGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LessonGroupRepository extends JpaRepository<LessonGroup, LessonGroupId> {
}
