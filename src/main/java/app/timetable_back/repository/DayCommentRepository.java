package app.timetable_back.repository;

import app.timetable_back.entity.DayComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DayCommentRepository extends JpaRepository<DayComment, Long> {
    
    List<DayComment> findByDate(LocalDate date);
}
