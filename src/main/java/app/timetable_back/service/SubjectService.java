package app.timetable_back.service;

import app.timetable_back.dto.PageResponse;
import app.timetable_back.dto.SubjectDto;
import app.timetable_back.dto.SubjectListViewDto;
import app.timetable_back.dto.SubjectResponseDto;
import app.timetable_back.entity.Subject;
import app.timetable_back.repository.SubjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @Transactional(readOnly = true)
    public Subject findById(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subject with id '" + id + "' not found"));
    }

    @Transactional
    public Subject createSubject(SubjectDto subjectDto) {
        Subject subject = Subject.builder()
                .name(subjectDto.getName())
                .code(subjectDto.getCode())
                .faculty(subjectDto.getFaculty())
                .description(subjectDto.getDescription())
                .build();

        return subjectRepository.save(subject);
    }

    @Transactional
    public Subject updateSubject(Long id, SubjectDto subjectDto) {
        Subject existingSubject = findById(id);

        existingSubject.setName(subjectDto.getName());
        existingSubject.setCode(subjectDto.getCode());
        existingSubject.setFaculty(subjectDto.getFaculty());
        existingSubject.setDescription(subjectDto.getDescription());

        return subjectRepository.save(existingSubject);
    }

    @Transactional
    public void deleteSubject(Long id) {
        if (!subjectRepository.existsById(id)) {
            throw new IllegalArgumentException("Subject with id '" + id + "' not found");
        }
        subjectRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Subject> findAll() {
        return subjectRepository.findAll();
    }

    /**
     * Create subject and return DTO
     */
    @Transactional
    public SubjectResponseDto createSubjectDto(SubjectDto subjectDto) {
        Subject subject = createSubject(subjectDto);
        return toDto(subject);
    }

    /**
     * Update subject and return DTO
     */
    @Transactional
    public SubjectResponseDto updateSubjectDto(Long id, SubjectDto subjectDto) {
        Subject subject = updateSubject(id, subjectDto);
        return toDto(subject);
    }

    /**
     * Get subject by ID as DTO
     */
    @Transactional(readOnly = true)
    public SubjectResponseDto findByIdDto(Long id) {
        Subject subject = findById(id);
        return toDto(subject);
    }

    /**
     * Get all subjects as DTOs
     */
    @Transactional(readOnly = true)
    public List<SubjectResponseDto> findAllDto() {
        return subjectRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Get paginated subjects as ListView DTOs (без id, createdAt, updatedAt)
     */
    @Transactional(readOnly = true)
    public PageResponse<SubjectListViewDto> findAllListView(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Subject> subjectPage = subjectRepository.findAll(pageable);

        List<SubjectListViewDto> content = subjectPage.getContent().stream()
                .map(this::toListViewDto)
                .collect(Collectors.toList());

        return PageResponse.<SubjectListViewDto>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(subjectPage.getTotalElements())
                .totalPages(subjectPage.getTotalPages())
                .build();
    }

    /**
     * Map Subject entity to SubjectResponseDto
     */
    private SubjectResponseDto toDto(Subject subject) {
        return SubjectResponseDto.builder()
                .id(subject.getId())
                .name(subject.getName())
                .code(subject.getCode())
                .faculty(subject.getFaculty())
                .description(subject.getDescription())
                .createdAt(subject.getCreatedAt())
                .updatedAt(subject.getUpdatedAt())
                .build();
    }

    /**
     * Map Subject entity to SubjectListViewDto (без id, createdAt, updatedAt)
     */
    private SubjectListViewDto toListViewDto(Subject subject) {
        return SubjectListViewDto.builder()
                .name(subject.getName())
                .code(subject.getCode())
                .faculty(subject.getFaculty())
                .description(subject.getDescription())
                .build();
    }
}
