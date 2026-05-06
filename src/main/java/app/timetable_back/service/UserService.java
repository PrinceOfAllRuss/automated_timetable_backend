package app.timetable_back.service;
import app.timetable_back.dto.PageResponse;
import app.timetable_back.dto.UserDto;
import app.timetable_back.dto.UserListViewDto;
import app.timetable_back.dto.UserResponseDto;
import app.timetable_back.entity.User;
import app.timetable_back.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with email '" + email + "' not found"));
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User with id '" + id + "' not found"));
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User createUser(UserDto userDto) {
        if (existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        User user = User.builder()
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .email(userDto.getEmail())
                .passwordHash(passwordEncoder.encode(userDto.getPassword()))
                .role(userDto.getRole())
                .phone(userDto.getPhone())
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, UserDto userDto) {
        User existingUser = findById(id);

        if (!existingUser.getEmail().equals(userDto.getEmail()) && existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        existingUser.setFirstName(userDto.getFirstName());
        existingUser.setLastName(userDto.getLastName());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setRole(userDto.getRole());
        existingUser.setPhone(userDto.getPhone());

        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            existingUser.setPasswordHash(passwordEncoder.encode(userDto.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User with id '" + id + "' not found");
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public UserResponseDto createUserDto(UserDto userDto) {
        User user = createUser(userDto);
        return toDto(user);
    }

    @Transactional
    public UserResponseDto updateUserDto(Long id, UserDto userDto) {
        User user = updateUser(id, userDto);
        return toDto(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDto findByIdDto(Long id) {
        User user = findById(id);
        return toDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> findAllDto() {
        return userRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageResponse<UserListViewDto> findAllListView(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        
        String searchPattern = (search != null && !search.trim().isEmpty()) 
                ? "%" + search.trim().toLowerCase() + "%" 
                : null;
                
        Page<User> userPage = userRepository.findBySearchQuery(searchPattern, pageable);

        List<UserListViewDto> content = userPage.getContent().stream()
                .map(this::toListViewDto)
                .collect(Collectors.toList());

        return PageResponse.<UserListViewDto>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .build();
    }

    private UserResponseDto toDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private UserListViewDto toListViewDto(User user) {
        return UserListViewDto.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .phone(user.getPhone())
                .build();
    }
}