package app.timetable_back.service;
import app.timetable_back.dto.GroupDto;
import app.timetable_back.dto.GroupListViewDto;
import app.timetable_back.dto.GroupResponseDto;
import app.timetable_back.dto.PageResponse;
import app.timetable_back.entity.StudentGroup;
import app.timetable_back.repository.GroupRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Transactional
    public GroupResponseDto createGroupDto(GroupDto groupDto) {
        StudentGroup group = createGroup(groupDto);
        return toDto(group);
    }

    @Transactional
    public GroupResponseDto updateGroupDto(Long id, GroupDto groupDto) {
        StudentGroup group = updateGroup(id, groupDto);
        return toDto(group);
    }

    @Transactional(readOnly = true)
    public GroupResponseDto findByIdDto(Long id) {
        StudentGroup group = findById(id);
        return toDto(group);
    }

    @Transactional(readOnly = true)
    public List<GroupResponseDto> findAllDto() {
        return groupRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<GroupListViewDto> findAllListView(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        String searchPattern = (search != null && !search.trim().isEmpty()) 
                ? "%" + search.trim().toLowerCase() + "%" 
                : null;

        Page<StudentGroup> groupPage = groupRepository.findBySearchQuery(searchPattern, pageable);
        List<GroupListViewDto> content = groupPage.getContent().stream()
                .map(this::toListViewDto)
                .collect(Collectors.toList());

        return PageResponse.<GroupListViewDto>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(groupPage.getTotalElements())
                .totalPages(groupPage.getTotalPages())
                .build();
    }

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

    private GroupListViewDto toListViewDto(StudentGroup group) {
        return GroupListViewDto.builder()
                .name(group.getName())
                .courseYear(group.getCourseYear())
                .studentCount(group.getStudentCount())
                .build();
    }
}