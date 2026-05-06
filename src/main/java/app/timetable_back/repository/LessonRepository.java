package app.timetable_back.repository;

import app.timetable_back.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    @Query("SELECT DISTINCT l FROM Lesson l " +
           "LEFT JOIN FETCH l.subject " +
           "LEFT JOIN FETCH l.teacher " +
           "LEFT JOIN FETCH l.lessonStudentGroups lsg " +
           "LEFT JOIN FETCH lsg.group " +
           "LEFT JOIN FETCH l.lessonRooms lr " +
           "LEFT JOIN FETCH lr.room " +
           "WHERE l.id = :id")
    Optional<Lesson> findByIdWithDetails(Long id);

    @Query("SELECT DISTINCT l FROM Lesson l " +
           "LEFT JOIN FETCH l.subject " +
           "LEFT JOIN FETCH l.teacher " +
           "LEFT JOIN FETCH l.lessonStudentGroups lsg " +
           "LEFT JOIN FETCH lsg.group " +
           "LEFT JOIN FETCH l.lessonRooms lr " +
           "LEFT JOIN FETCH lr.room")
    List<Lesson> findAllWithDetails();

    @Query("SELECT DISTINCT l FROM Lesson l " +
           "LEFT JOIN FETCH l.subject " +
           "LEFT JOIN FETCH l.teacher " +
           "LEFT JOIN FETCH l.lessonStudentGroups lsg " +
           "LEFT JOIN FETCH lsg.group " +
           "LEFT JOIN FETCH l.lessonRooms lr " +
           "LEFT JOIN FETCH lr.room " +
           "WHERE l.startAt >= :startDate AND l.startAt <= :endDate " +
           "ORDER BY l.startAt")
    List<Lesson> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}
