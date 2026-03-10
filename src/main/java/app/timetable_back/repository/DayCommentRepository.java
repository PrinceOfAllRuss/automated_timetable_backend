package app.timetable_back.repository;

import app.timetable_back.entity.DayComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DayCommentRepository extends JpaRepository<DayComment, Long> {
}
