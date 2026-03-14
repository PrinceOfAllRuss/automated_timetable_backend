package app.timetable_back.service;

import app.timetable_back.dto.GroupDto;
import app.timetable_back.dto.GroupResponseDto;
import app.timetable_back.entity.StudentGroup;
import app.timetable_back.repository.GroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    @Transactional(readOnly = true)
    public StudentGroup findById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Group with id '" + id + "' not found"));
    }

    @Transactional
    public StudentGroup createGroup(GroupDto groupDto) {
        StudentGroup group = StudentGroup.builder()
                .name(groupDto.getName())
                .courseYear(groupDto.getCourseYear())
                .studentCount(groupDto.getStudentCount())
                .build();

        return groupRepository.save(group);
    }

    @Transactional
    public StudentGroup updateGroup(Long id, GroupDto groupDto) {
        StudentGroup existingGroup = findById(id);

        existingGroup.setName(groupDto.getName());
        existingGroup.setCourseYear(groupDto.getCourseYear());
        existingGroup.setStudentCount(groupDto.getStudentCount());

        return groupRepository.save(existingGroup);
    }

    @Transactional
    public void deleteGroup(Long id) {
        if (!groupRepository.existsById(id)) {
            throw new IllegalArgumentException("Group with id '" + id + "' not found");
        }
        groupRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<StudentGroup> findAll() {
        return groupRepository.findAll();
    }

    /**
     * Create group and return DTO
     */
    @Transactional
    public GroupResponseDto createGroupDto(GroupDto groupDto) {
        StudentGroup group = createGroup(groupDto);
        return toDto(group);
    }

    /**
     * Update group and return DTO
     */
    @Transactional
    public GroupResponseDto updateGroupDto(Long id, GroupDto groupDto) {
        StudentGroup group = updateGroup(id, groupDto);
        return toDto(group);
    }

    /**
     * Get group by ID as DTO
     */
    @Transactional(readOnly = true)
    public GroupResponseDto findByIdDto(Long id) {
        StudentGroup group = findById(id);
        return toDto(group);
    }

    /**
     * Get all groups as DTOs
     */
    @Transactional(readOnly = true)
    public List<GroupResponseDto> findAllDto() {
        return groupRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Map StudentGroup entity to GroupResponseDto
     */
    private GroupResponseDto toDto(StudentGroup group) {
        return GroupResponseDto.builder()
                .id(group.getId())
                .name(group.getName())
                .courseYear(group.getCourseYear())
                .studentCount(group.getStudentCount())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }
}
