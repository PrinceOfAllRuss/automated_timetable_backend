package app.timetable_back.service;

import app.timetable_back.entity.Lesson;
import app.timetable_back.entity.LessonGroup;
import app.timetable_back.entity.LessonGroupId;
import app.timetable_back.repository.LessonGroupRepository;
import app.timetable_back.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonService {

    private final LessonRepository lessonRepository;
    private final LessonGroupRepository lessonGroupRepository;
    private final ScheduleValidationService validationService;

    public Lesson createLesson(Lesson lesson, List<Long> groupIds) {
        // Проверка: teacher_id должен быть преподавателем
        if (lesson.getTeacher() != null && lesson.getTeacher().getId() != null) {
            validationService.validateTeacherRole(lesson.getTeacher().getId());
        }

        // Проверка: вместимость аудитории
        if (lesson.getRoom() != null && lesson.getRoom().getId() != null && groupIds != null) {
            if (!validationService.isRoomCapacitySufficientForGroups(
                    lesson.getRoom().getId(), groupIds)) {
                throw new IllegalArgumentException(
                    "Вместимость аудитории недостаточна для указанного количества студентов"
                );
            }
        }

        Lesson savedLesson = lessonRepository.save(lesson);

        // Связываем с группами
        if (groupIds != null) {
            for (Long groupId : groupIds) {
                // Проверка на конфликт по группе
                if (validationService.hasGroupConflict(
                        groupId,
                        savedLesson.getStartAt(),
                        savedLesson.getEndAt())) {
                    throw new IllegalArgumentException(
                        "Обнаружен конфликт по времени для группы " + groupId
                    );
                }

                LessonGroupId id = new LessonGroupId(savedLesson.getId(), groupId);
                LessonGroup lessonGroup = LessonGroup.builder()
                        .id(id)
                        .lesson(savedLesson)
                        .build();
                lessonGroupRepository.save(lessonGroup);
            }
        }

        return savedLesson;
    }

    // Обновляет существующее занятие с проверками
    public Lesson updateLesson(Long lessonId, Lesson updatedLesson, List<Long> groupIds) {
        Lesson existingLesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Занятие с ID " + lessonId + " не найдено"));

        // Проверка: teacher_id должен быть преподавателем
        if (updatedLesson.getTeacher() != null && updatedLesson.getTeacher().getId() != null) {
            validationService.validateTeacherRole(updatedLesson.getTeacher().getId());
        }

        // Проверка: вместимость аудитории
        if (updatedLesson.getRoom() != null && updatedLesson.getRoom().getId() != null && groupIds != null) {
            if (!validationService.isRoomCapacitySufficientForGroups(
                    updatedLesson.getRoom().getId(), groupIds)) {
                throw new IllegalArgumentException(
                    "Вместимость аудитории недостаточна для указанного количества студентов"
                );
            }
        }

        // Обновляем поля
        existingLesson.setStartAt(updatedLesson.getStartAt());
        existingLesson.setEndAt(updatedLesson.getEndAt());
        existingLesson.setRoom(updatedLesson.getRoom());
        existingLesson.setSubject(updatedLesson.getSubject());
        existingLesson.setTeacher(updatedLesson.getTeacher());
        existingLesson.setRecurrence(updatedLesson.getRecurrence());
        existingLesson.setIsOverride(updatedLesson.getIsOverride());
        existingLesson.setIsCancelled(updatedLesson.getIsCancelled());

        Lesson savedLesson = lessonRepository.save(existingLesson);

        // Обновляем связи с группами
        if (groupIds != null) {
            // Удаляем старые связи
            lessonGroupRepository.deleteById(new LessonGroupId(lessonId, null));

            for (Long groupId : groupIds) {
                // Проверка на конфликт по группе (исключая текущее занятие)
                if (validationService.hasGroupConflict(
                        groupId,
                        savedLesson.getStartAt(),
                        savedLesson.getEndAt(),
                        lessonId)) {
                    throw new IllegalArgumentException(
                        "Обнаружен конфликт по времени для группы " + groupId
                    );
                }

                LessonGroupId id = new LessonGroupId(savedLesson.getId(), groupId);
                LessonGroup lessonGroup = LessonGroup.builder()
                        .id(id)
                        .lesson(savedLesson)
                        .build();
                lessonGroupRepository.save(lessonGroup);
            }
        }

        return savedLesson;
    }

    // Удаляет занятие по ID
    public void deleteLesson(Long lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new IllegalArgumentException("Занятие с ID " + lessonId + " не найдено");
        }
        lessonRepository.deleteById(lessonId);
    }
}
