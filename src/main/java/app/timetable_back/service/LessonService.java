package app.timetable_back.service;
import app.timetable_back.dto.*;
import app.timetable_back.entity.*;
import app.timetable_back.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonService {
    private final LessonRepository lessonRepository;
    private final LessonRoomRepository lessonRoomRepository;
    private final LessonStudentGroupRepository lessonStudentGroupRepository;
    private final GroupRepository groupRepository;
    private final RoomRepository roomRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final ScheduleValidationService validationService;

    public Lesson createLesson(LessonDto lessonDto) {
        if (lessonDto.getEndAt().isBefore(lessonDto.getStartAt()) ||
            lessonDto.getEndAt().isEqual(lessonDto.getStartAt())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        Lesson.LessonBuilder builder = Lesson.builder()
                .startAt(lessonDto.getStartAt())
                .endAt(lessonDto.getEndAt())
                .isOverride(lessonDto.getIsOverride())
                .isCancelled(lessonDto.getIsCancelled())
                .ruleType(lessonDto.getRuleType());

        if (lessonDto.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(lessonDto.getSubjectId())
                    .orElseThrow(() -> new IllegalArgumentException("Subject with id '" + lessonDto.getSubjectId() + "' not found"));
            builder.subject(subject);
        }

        if (lessonDto.getTeacherId() != null) {
            User teacher = userRepository.findById(lessonDto.getTeacherId())
                    .orElseThrow(() -> new IllegalArgumentException("User with id '" + lessonDto.getTeacherId() + "' not found"));
            validationService.validateTeacherRole(teacher.getId());
            builder.teacher(teacher);
        }

        Lesson lesson = builder.build();
        Lesson savedLesson = lessonRepository.save(lesson);

        if (lessonDto.getRoomIds() != null && !lessonDto.getRoomIds().isEmpty()) {
            for (Long roomId : lessonDto.getRoomIds()) {
                Room room = roomRepository.findById(roomId)
                        .orElseThrow(() -> new IllegalArgumentException("Room with id '" + roomId + "' not found"));
                
                if (lessonDto.getGroupIds() != null && !lessonDto.getGroupIds().isEmpty()) {
                    if (!validationService.isRoomCapacitySufficientForGroups(roomId, lessonDto.getGroupIds())) {
                        throw new IllegalArgumentException("Room capacity is insufficient for the specified number of students in room " + roomId);
                    }
                }

                LessonRoomId lrId = new LessonRoomId(savedLesson.getId(), roomId);
                LessonRoom lessonRoom = LessonRoom.builder()
                        .id(lrId).lesson(savedLesson).room(room).build();
                lessonRoomRepository.save(lessonRoom);
            }
        }

        if (lessonDto.getGroupIds() != null && !lessonDto.getGroupIds().isEmpty()) {
            for (Long groupId : lessonDto.getGroupIds()) {
                StudentGroup group = groupRepository.findById(groupId)
                        .orElseThrow(() -> new IllegalArgumentException("Group with id '" + groupId + "' not found"));

                if (validationService.hasGroupConflict(groupId, savedLesson.getStartAt(), savedLesson.getEndAt())) {
                    throw new IllegalArgumentException("Group schedule conflict detected for group " + groupId);
                }

                LessonStudentGroupId id = new LessonStudentGroupId(savedLesson.getId(), groupId);
                LessonStudentGroup lsg = LessonStudentGroup.builder()
                        .id(id).lesson(savedLesson).group(group).build();
                lessonStudentGroupRepository.save(lsg);
            }
        }

        return savedLesson;
    }

    public Lesson updateLesson(Long lessonId, LessonDto lessonDto) {
        Lesson existingLesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson with id '" + lessonId + "' not found"));

        if (lessonDto.getEndAt().isBefore(lessonDto.getStartAt()) ||
            lessonDto.getEndAt().isEqual(lessonDto.getStartAt())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        existingLesson.setStartAt(lessonDto.getStartAt());
        existingLesson.setEndAt(lessonDto.getEndAt());
        existingLesson.setIsOverride(lessonDto.getIsOverride());
        existingLesson.setIsCancelled(lessonDto.getIsCancelled());
        existingLesson.setRuleType(lessonDto.getRuleType());

        if (lessonDto.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(lessonDto.getSubjectId())
                    .orElseThrow(() -> new IllegalArgumentException("Subject with id '" + lessonDto.getSubjectId() + "' not found"));
            existingLesson.setSubject(subject);
        }

        if (lessonDto.getTeacherId() != null) {
            User teacher = userRepository.findById(lessonDto.getTeacherId())
                    .orElseThrow(() -> new IllegalArgumentException("User with id '" + lessonDto.getTeacherId() + "' not found"));
            validationService.validateTeacherRole(teacher.getId());
            existingLesson.setTeacher(teacher);
        }

        Lesson savedLesson = lessonRepository.save(existingLesson);

        if (lessonDto.getRoomIds() != null) {
            lessonRoomRepository.deleteByLessonId(lessonId);
            for (Long roomId : lessonDto.getRoomIds()) {
                Room room = roomRepository.findById(roomId)
                        .orElseThrow(() -> new IllegalArgumentException("Room with id '" + roomId + "' not found"));
                
                if (lessonDto.getGroupIds() != null && !lessonDto.getGroupIds().isEmpty()) {
                    if (!validationService.isRoomCapacitySufficientForGroups(roomId, lessonDto.getGroupIds())) {
                        throw new IllegalArgumentException("Room capacity is insufficient for the specified number of students in room " + roomId);
                    }
                }

                LessonRoomId lrId = new LessonRoomId(savedLesson.getId(), roomId);
                LessonRoom lessonRoom = LessonRoom.builder()
                        .id(lrId).lesson(savedLesson).room(room).build();
                lessonRoomRepository.save(lessonRoom);
            }
        }

        if (lessonDto.getGroupIds() != null) {
            lessonStudentGroupRepository.deleteAll(existingLesson.getLessonStudentGroups());
            for (Long groupId : lessonDto.getGroupIds()) {
                StudentGroup group = groupRepository.findById(groupId)
                        .orElseThrow(() -> new IllegalArgumentException("Group with id '" + groupId + "' not found"));

                if (validationService.hasGroupConflict(groupId, savedLesson.getStartAt(), savedLesson.getEndAt(), lessonId)) {
                    throw new IllegalArgumentException("Group schedule conflict detected for group " + groupId);
                }

                LessonStudentGroupId id = new LessonStudentGroupId(savedLesson.getId(), groupId);
                LessonStudentGroup lsg = LessonStudentGroup.builder()
                        .id(id).lesson(savedLesson).group(group).build();
                lessonStudentGroupRepository.save(lsg);
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
        return new ArrayList<>(lessonRepository.findAllWithDetails());
    }

    public LessonResponseDto createLessonDto(LessonDto lessonDto) {
        Lesson lesson = createLesson(lessonDto);
        return toDto(lesson);
    }

    public LessonResponseDto updateLessonDto(Long lessonId, LessonDto lessonDto) {
        Lesson lesson = updateLesson(lessonId, lessonDto);
        return toDto(lesson);
    }

    @Transactional(readOnly = true)
    public List<LessonResponseDto> findAllDto() {
        return lessonRepository.findAllWithDetails().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<LessonListViewDto> findAllListView(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Lesson> lessonPage = lessonRepository.findAll(pageable);
        
        List<LessonListViewDto> content = lessonPage.getContent().stream()
                .map(this::toListViewDto)
                .collect(Collectors.toList());

        return PageResponse.<LessonListViewDto>builder()
                .content(content).page(page).size(size)
                .totalElements(lessonPage.getTotalElements())
                .totalPages(lessonPage.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public LessonResponseDto findByIdDto(Long lessonId) {
        Lesson lesson = lessonRepository.findByIdWithDetails(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson with id '" + lessonId + "' not found"));
        return toDto(lesson);
    }

    // --- MAPPER METHODS (используют существующие DTO, без вложенных классов) ---

    private LessonResponseDto toDto(Lesson lesson) {
        List<Long> groupIds = lesson.getLessonStudentGroups() != null
                ? new ArrayList<>(lesson.getLessonStudentGroups().stream()
                    .map(lsg -> lsg.getGroup().getId())
                    .collect(Collectors.toSet()))
                : new ArrayList<>();

        List<RoomResponseDto> rooms = lesson.getLessonRooms() != null
                ? lesson.getLessonRooms().stream()
                    .map(lr -> RoomResponseDto.builder()
                            .id(lr.getRoom().getId())
                            .roomNumber(lr.getRoom().getRoomNumber())
                            .building(lr.getRoom().getBuilding())
                            .capacity(lr.getRoom().getCapacity())
                            .createdAt(lr.getRoom().getCreatedAt())
                            .updatedAt(lr.getRoom().getUpdatedAt())
                            .build())
                    .collect(Collectors.toList())
                : Collections.emptyList();

        SubjectResponseDto subject = lesson.getSubject() != null
                ? SubjectResponseDto.builder()
                    .id(lesson.getSubject().getId())
                    .name(lesson.getSubject().getName())
                    .code(lesson.getSubject().getCode())
                    .faculty(lesson.getSubject().getFaculty())
                    .description(lesson.getSubject().getDescription())
                    .createdAt(lesson.getSubject().getCreatedAt())
                    .updatedAt(lesson.getSubject().getUpdatedAt())
                    .build()
                : null;

        UserResponseDto teacher = lesson.getTeacher() != null
                ? UserResponseDto.builder()
                    .id(lesson.getTeacher().getId())
                    .firstName(lesson.getTeacher().getFirstName())
                    .lastName(lesson.getTeacher().getLastName())
                    .email(lesson.getTeacher().getEmail())
                    .role(lesson.getTeacher().getRole())
                    .phone(lesson.getTeacher().getPhone())
                    .createdAt(lesson.getTeacher().getCreatedAt())
                    .updatedAt(lesson.getTeacher().getUpdatedAt())
                    .build()
                : null;

        return LessonResponseDto.builder()
                .id(lesson.getId())
                .startAt(lesson.getStartAt())
                .endAt(lesson.getEndAt())
                .ruleType(lesson.getRuleType())
                .isOverride(lesson.getIsOverride())
                .isCancelled(lesson.getIsCancelled())
                .createdAt(lesson.getCreatedAt() != null ? lesson.getCreatedAt().toLocalDateTime() : null)
                .updatedAt(lesson.getUpdatedAt() != null ? lesson.getUpdatedAt().toLocalDateTime() : null)
                .rooms(rooms)
                .subject(subject)
                .teacher(teacher)
                .groupIds(groupIds)
                .build();
    }

    private LessonListViewDto toListViewDto(Lesson lesson) {
        List<Long> groupIds = lesson.getLessonStudentGroups() != null
                ? new ArrayList<>(lesson.getLessonStudentGroups().stream()
                    .map(lsg -> lsg.getGroup().getId())
                    .collect(Collectors.toSet()))
                : new ArrayList<>();

        List<RoomListViewDto> rooms = lesson.getLessonRooms() != null
                ? lesson.getLessonRooms().stream()
                    .map(lr -> RoomListViewDto.builder()
                            .roomNumber(lr.getRoom().getRoomNumber())
                            .building(lr.getRoom().getBuilding())
                            .capacity(lr.getRoom().getCapacity())
                            .build())
                    .collect(Collectors.toList())
                : Collections.emptyList();

        SubjectListViewDto subject = lesson.getSubject() != null
                ? SubjectListViewDto.builder()
                    .name(lesson.getSubject().getName())
                    .code(lesson.getSubject().getCode())
                    .faculty(lesson.getSubject().getFaculty())
                    .description(lesson.getSubject().getDescription())
                    .build()
                : null;

        UserListViewDto teacher = lesson.getTeacher() != null
                ? UserListViewDto.builder()
                    .firstName(lesson.getTeacher().getFirstName())
                    .lastName(lesson.getTeacher().getLastName())
                    .email(lesson.getTeacher().getEmail())
                    .role(lesson.getTeacher().getRole())
                    .phone(lesson.getTeacher().getPhone())
                    .build()
                : null;

        return LessonListViewDto.builder()
                .startAt(lesson.getStartAt())
                .endAt(lesson.getEndAt())
                .ruleType(lesson.getRuleType())
                .isOverride(lesson.getIsOverride())
                .isCancelled(lesson.getIsCancelled())
                .rooms(rooms)
                .subject(subject)
                .teacher(teacher)
                .groupIds(groupIds)
                .build();
    }
}