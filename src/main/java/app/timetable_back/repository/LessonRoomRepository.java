package app.timetable_back.repository;

import app.timetable_back.entity.LessonRoom;
import app.timetable_back.entity.LessonRoomId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRoomRepository extends JpaRepository<LessonRoom, LessonRoomId> {
    
    @Query("SELECT lr FROM LessonRoom lr JOIN FETCH lr.room WHERE lr.lesson.id = :lessonId")
    List<LessonRoom> findByLessonIdWithRoom(Long lessonId);
    
    void deleteByLessonId(Long lessonId);
}