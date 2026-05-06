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

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Transactional(readOnly = true)
    public List<LessonResponseDto> findLessonsByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return lessonRepository.findByDateRange(startOfDay, endOfDay).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LessonResponseDto> findLessonsByDateRange(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be after or equal to start date");
        }
        // Преобразуем даты в LocalTime: начало дня 00:00:00 и конец дня 23:59:59
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        
        return lessonRepository.findByDateRange(startDateTime, endDateTime).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private LessonResponseDto toDto(Lesson lesson) {
        List<Long> groupIds = lesson.getLessonStudentGroups() != null
                ? lesson.getLessonStudentGroups().stream()
                    .map(lsg -> lsg.getGroup().getId())
                    .collect(Collectors.toList())
                : List.of();

        List<RoomResponseDto> rooms = lesson.getLessonRooms() != null
                ? lesson.getLessonRooms().stream()
                    .map(lr -> mapRoomResponse(lr.getRoom()))
                    .collect(Collectors.toList())
                : List.of();

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
                .subject(lesson.getSubject() != null ? mapSubjectResponse(lesson.getSubject()) : null)
                .teacher(lesson.getTeacher() != null ? mapUserResponse(lesson.getTeacher()) : null)
                .groupIds(groupIds)
                .build();
    }

    private LessonListViewDto toListViewDto(Lesson lesson) {
        List<Long> groupIds = lesson.getLessonStudentGroups() != null
                ? lesson.getLessonStudentGroups().stream()
                    .map(lsg -> lsg.getGroup().getId())
                    .collect(Collectors.toList())
                : List.of();

        List<RoomListViewDto> rooms = lesson.getLessonRooms() != null
                ? lesson.getLessonRooms().stream()
                    .map(lr -> mapRoomListView(lr.getRoom()))
                    .collect(Collectors.toList())
                : List.of();

        return LessonListViewDto.builder()
                .startAt(lesson.getStartAt())
                .endAt(lesson.getEndAt())
                .ruleType(lesson.getRuleType())
                .isOverride(lesson.getIsOverride())
                .isCancelled(lesson.getIsCancelled())
                .rooms(rooms)
                .subject(lesson.getSubject() != null ? mapSubjectListView(lesson.getSubject()) : null)
                .teacher(lesson.getTeacher() != null ? mapUserListView(lesson.getTeacher()) : null)
                .groupIds(groupIds)
                .build();
    }

    private RoomResponseDto mapRoomResponse(Room room) {
        if (room == null) return null;
        return RoomResponseDto.builder().id(room.getId()).roomNumber(room.getRoomNumber())
                .building(room.getBuilding()).capacity(room.getCapacity())
                .createdAt(room.getCreatedAt()).updatedAt(room.getUpdatedAt()).build();
    }
    private RoomListViewDto mapRoomListView(Room room) {
        if (room == null) return null;
        return RoomListViewDto.builder().roomNumber(room.getRoomNumber())
                .building(room.getBuilding()).capacity(room.getCapacity()).build();
    }
    private SubjectResponseDto mapSubjectResponse(Subject subject) {
        if (subject == null) return null;
        return SubjectResponseDto.builder().id(subject.getId()).name(subject.getName())
                .code(subject.getCode()).faculty(subject.getFaculty()).description(subject.getDescription())
                .createdAt(subject.getCreatedAt()).updatedAt(subject.getUpdatedAt()).build();
    }
    private SubjectListViewDto mapSubjectListView(Subject subject) {
        if (subject == null) return null;
        return SubjectListViewDto.builder().name(subject.getName()).code(subject.getCode())
                .faculty(subject.getFaculty()).description(subject.getDescription()).build();
    }
    private UserResponseDto mapUserResponse(User user) {
        if (user == null) return null;
        return UserResponseDto.builder().id(user.getId()).firstName(user.getFirstName())
                .lastName(user.getLastName()).email(user.getEmail()).role(user.getRole())
                .phone(user.getPhone()).createdAt(user.getCreatedAt()).updatedAt(user.getUpdatedAt()).build();
    }
    private UserListViewDto mapUserListView(User user) {
        if (user == null) return null;
        return UserListViewDto.builder().firstName(user.getFirstName()).lastName(user.getLastName())
                .email(user.getEmail()).role(user.getRole()).phone(user.getPhone()).build();
    }
}