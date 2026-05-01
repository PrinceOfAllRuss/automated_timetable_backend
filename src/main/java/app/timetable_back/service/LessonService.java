package app.timetable_back.service;

import app.timetable_back.dto.LessonDto;
import app.timetable_back.dto.LessonListViewDto;
import app.timetable_back.dto.LessonResponseDto;
import app.timetable_back.dto.PageResponse;
import app.timetable_back.entity.*;
import app.timetable_back.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonService {

    private final LessonRepository lessonRepository;
    private final LessonStudentGroupRepository lessonStudentGroupRepository;
    private final GroupRepository groupRepository;
    private final RoomRepository roomRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final ScheduleValidationService validationService;

    public Lesson createLesson(LessonDto lessonDto) {
        // Validate end time is after start time
        if (lessonDto.getEndAt().isBefore(lessonDto.getStartAt()) ||
            lessonDto.getEndAt().isEqual(lessonDto.getStartAt())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        // Build lesson from DTO
        Lesson.LessonBuilder builder = Lesson.builder()
                .startAt(lessonDto.getStartAt())
                .endAt(lessonDto.getEndAt())
                .isOverride(lessonDto.getIsOverride())
                .isCancelled(lessonDto.getIsCancelled())
                .ruleType(lessonDto.getRuleType());

        // Set room if provided
        if (lessonDto.getRoomId() != null) {
            Room room = roomRepository.findById(lessonDto.getRoomId())
                    .orElseThrow(() -> new IllegalArgumentException("Room with id '" + lessonDto.getRoomId() + "' not found"));
            builder.room(room);
        }

        // Set subject if provided
        if (lessonDto.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(lessonDto.getSubjectId())
                    .orElseThrow(() -> new IllegalArgumentException("Subject with id '" + lessonDto.getSubjectId() + "' not found"));
            builder.subject(subject);
        }

        // Set teacher if provided
        if (lessonDto.getTeacherId() != null) {
            User teacher = userRepository.findById(lessonDto.getTeacherId())
                    .orElseThrow(() -> new IllegalArgumentException("User with id '" + lessonDto.getTeacherId() + "' not found"));

            // Validate teacher role
            validationService.validateTeacherRole(teacher.getId());
            builder.teacher(teacher);
        }

        Lesson lesson = builder.build();

        // Validate room capacity if groups are provided
        if (lessonDto.getGroupIds() != null && !lessonDto.getGroupIds().isEmpty() && lesson.getRoom() != null) {
            if (!validationService.isRoomCapacitySufficientForGroups(
                    lesson.getRoom().getId(), lessonDto.getGroupIds())) {
                throw new IllegalArgumentException(
                    "Room capacity is insufficient for the specified number of students"
                );
            }
        }

        // Save lesson first
        Lesson savedLesson = lessonRepository.save(lesson);

        // Create lesson-group associations
        if (lessonDto.getGroupIds() != null && !lessonDto.getGroupIds().isEmpty()) {
            for (Long groupId : lessonDto.getGroupIds()) {
                StudentGroup group = groupRepository.findById(groupId)
                        .orElseThrow(() -> new IllegalArgumentException("Group with id '" + groupId + "' not found"));

                // Check for group time conflict
                if (validationService.hasGroupConflict(
                        groupId,
                        savedLesson.getStartAt(),
                        savedLesson.getEndAt())) {
                    throw new IllegalArgumentException(
                        "Group schedule conflict detected for group " + groupId
                    );
                }

                LessonStudentGroupId id = new LessonStudentGroupId(savedLesson.getId(), groupId);
                LessonStudentGroup lessonStudentGroup = LessonStudentGroup.builder()
                        .id(id)
                        .lesson(savedLesson)
                        .group(group)
                        .build();
                lessonStudentGroupRepository.save(lessonStudentGroup);
            }
        }

        return savedLesson;
    }

    public Lesson updateLesson(Long lessonId, LessonDto lessonDto) {
        Lesson existingLesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson with id '" + lessonId + "' not found"));

        // Validate end time is after start time
        if (lessonDto.getEndAt().isBefore(lessonDto.getStartAt()) ||
            lessonDto.getEndAt().isEqual(lessonDto.getStartAt())) {
            throw new IllegalArgumentException("End time must be after start time");
        }
        existingLesson.setStartAt(lessonDto.getStartAt());
        existingLesson.setEndAt(lessonDto.getEndAt());
        existingLesson.setIsOverride(lessonDto.getIsOverride());
        existingLesson.setIsCancelled(lessonDto.getIsCancelled());
        existingLesson.setRuleType(lessonDto.getRuleType());

        // Update room
        if (lessonDto.getRoomId() != null) {
            Room room = roomRepository.findById(lessonDto.getRoomId())
                    .orElseThrow(() -> new IllegalArgumentException("Room with id '" + lessonDto.getRoomId() + "' not found"));
            existingLesson.setRoom(room);
        }

        // Update subject
        if (lessonDto.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(lessonDto.getSubjectId())
                    .orElseThrow(() -> new IllegalArgumentException("Subject with id '" + lessonDto.getSubjectId() + "' not found"));
            existingLesson.setSubject(subject);
        }

        // Update teacher
        if (lessonDto.getTeacherId() != null) {
            User teacher = userRepository.findById(lessonDto.getTeacherId())
                    .orElseThrow(() -> new IllegalArgumentException("User with id '" + lessonDto.getTeacherId() + "' not found"));
            validationService.validateTeacherRole(teacher.getId());
            existingLesson.setTeacher(teacher);
        }

        Lesson savedLesson = lessonRepository.save(existingLesson);

        // Update lesson-student-group associations
        if (lessonDto.getGroupIds() != null) {
            // Remove old associations
            lessonStudentGroupRepository.deleteAll(existingLesson.getLessonStudentGroups());

            for (Long groupId : lessonDto.getGroupIds()) {
                StudentGroup group = groupRepository.findById(groupId)
                        .orElseThrow(() -> new IllegalArgumentException("Group with id '" + groupId + "' not found"));

                if (validationService.hasGroupConflict(
                        groupId,
                        savedLesson.getStartAt(),
                        savedLesson.getEndAt(),
                        lessonId)) {
                    throw new IllegalArgumentException(
                        "Group schedule conflict detected for group " + groupId
                    );
                }

                LessonStudentGroupId id = new LessonStudentGroupId(savedLesson.getId(), groupId);
                LessonStudentGroup lessonStudentGroup = LessonStudentGroup.builder()
                        .id(id)
                        .lesson(savedLesson)
                        .group(group)
                        .build();
                lessonStudentGroupRepository.save(lessonStudentGroup);
            }
        }

        return savedLesson;
    }

    public void deleteLesson(Long lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new IllegalArgumentException("Lesson with id '" + lessonId + "' not found");
        }
        lessonRepository.deleteById(lessonId);
    }

    public Lesson findById(Long lessonId) {
        return lessonRepository.findByIdWithDetails(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson with id '" + lessonId + "' not found"));
    }

    public List<Lesson> findAll() {
        return lessonRepository.findAllWithDetails();
    }

    /**
     * Create lesson and return DTO
     */
    public LessonResponseDto createLessonDto(LessonDto lessonDto) {
        Lesson lesson = createLesson(lessonDto);
        return toDto(lesson);
    }

    /**
     * Update lesson and return DTO
     */
    public LessonResponseDto updateLessonDto(Long lessonId, LessonDto lessonDto) {
        Lesson lesson = updateLesson(lessonId, lessonDto);
        return toDto(lesson);
    }

    /**
     * Get all lessons as DTOs
     */
    @Transactional(readOnly = true)
    public List<LessonResponseDto> findAllDto() {
        return lessonRepository.findAllWithDetails().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated lessons as ListView DTOs (без id, createdAt, updatedAt)
     */
    @Transactional(readOnly = true)
    public PageResponse<LessonListViewDto> findAllListView(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        // Используем стандартный findAll с Pageable
        Page<Lesson> lessonPage = lessonRepository.findAll(pageable);

        List<LessonListViewDto> content = lessonPage.getContent().stream()
                .map(this::toListViewDto)
                .collect(Collectors.toList());

        return PageResponse.<LessonListViewDto>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(lessonPage.getTotalElements())
                .totalPages(lessonPage.getTotalPages())
                .build();
    }

    /**
     * Get lesson by ID as DTO
     */
    @Transactional(readOnly = true)
    public LessonResponseDto findByIdDto(Long lessonId) {
        Lesson lesson = lessonRepository.findByIdWithDetails(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson with id '" + lessonId + "' not found"));
        return toDto(lesson);
    }

    /**
     * Map Lesson entity to LessonResponseDto
     */
    private LessonResponseDto toDto(Lesson lesson) {
        List<Long> groupIds = lesson.getLessonStudentGroups() != null
                ? lesson.getLessonStudentGroups().stream()
                    .map(lsg -> lsg.getGroup().getId())
                    .collect(Collectors.toList())
                : new ArrayList<>();

        return LessonResponseDto.builder()
                .id(lesson.getId())
                .startAt(lesson.getStartAt())
                .endAt(lesson.getEndAt())
                .ruleType(lesson.getRuleType())
                .isOverride(lesson.getIsOverride())
                .isCancelled(lesson.getIsCancelled())
                .createdAt(lesson.getCreatedAt() != null ? lesson.getCreatedAt().toLocalDateTime() : null)
                .updatedAt(lesson.getUpdatedAt() != null ? lesson.getUpdatedAt().toLocalDateTime() : null)
                .room(mapRoom(lesson.getRoom()))
                .subject(mapSubject(lesson.getSubject()))
                .teacher(mapTeacher(lesson.getTeacher()))
                .groupIds(groupIds)
                .build();
    }

    private LessonResponseDto.RoomInfo mapRoom(Room room) {
        if (room == null) return null;
        return LessonResponseDto.RoomInfo.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .building(room.getBuilding())
                .capacity(room.getCapacity())
                .build();
    }

    private LessonResponseDto.SubjectInfo mapSubject(Subject subject) {
        if (subject == null) return null;
        return LessonResponseDto.SubjectInfo.builder()
                .id(subject.getId())
                .name(subject.getName())
                .code(subject.getCode())
                .faculty(subject.getFaculty())
                .build();
    }

    private LessonResponseDto.TeacherInfo mapTeacher(User teacher) {
        if (teacher == null) return null;
        return LessonResponseDto.TeacherInfo.builder()
                .id(teacher.getId())
                .firstName(teacher.getFirstName())
                .lastName(teacher.getLastName())
                .email(teacher.getEmail())
                .build();
    }

    /**
     * Map Lesson entity to LessonListViewDto (без id, createdAt, updatedAt)
     */
    private LessonListViewDto toListViewDto(Lesson lesson) {
        List<Long> groupIds = lesson.getLessonStudentGroups() != null
                ? lesson.getLessonStudentGroups().stream()
                    .map(lsg -> lsg.getGroup().getId())
                    .collect(Collectors.toList())
                : new ArrayList<>();

        return LessonListViewDto.builder()
                .startAt(lesson.getStartAt())
                .endAt(lesson.getEndAt())
                .ruleType(lesson.getRuleType())
                .isOverride(lesson.getIsOverride())
                .isCancelled(lesson.getIsCancelled())
                .groupIds(groupIds)
                .room(mapRoomListView(lesson.getRoom()))
                .subject(mapSubjectListView(lesson.getSubject()))
                .teacher(mapTeacherListView(lesson.getTeacher()))
                .build();
    }

    private LessonListViewDto.RoomInfo mapRoomListView(Room room) {
        if (room == null) return null;
        return LessonListViewDto.RoomInfo.builder()
                .roomNumber(room.getRoomNumber())
                .building(room.getBuilding())
                .capacity(room.getCapacity())
                .build();
    }

    private LessonListViewDto.SubjectInfo mapSubjectListView(Subject subject) {
        if (subject == null) return null;
        return LessonListViewDto.SubjectInfo.builder()
                .name(subject.getName())
                .code(subject.getCode())
                .faculty(subject.getFaculty())
                .build();
    }

    private LessonListViewDto.TeacherInfo mapTeacherListView(User teacher) {
        if (teacher == null) return null;
        return LessonListViewDto.TeacherInfo.builder()
                .firstName(teacher.getFirstName())
                .lastName(teacher.getLastName())
                .email(teacher.getEmail())
                .build();
    }
}
