package app.timetable_back.repository;

import app.timetable_back.entity.StudentGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends JpaRepository<StudentGroup, Long> {
}
