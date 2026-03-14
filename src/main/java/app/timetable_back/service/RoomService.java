package app.timetable_back.service;

import app.timetable_back.dto.RoomDto;
import app.timetable_back.dto.RoomResponseDto;
import app.timetable_back.entity.Room;
import app.timetable_back.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Transactional(readOnly = true)
    public Room findById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room with id '" + id + "' not found"));
    }

    @Transactional
    public Room createRoom(RoomDto roomDto) {
        Room room = Room.builder()
                .roomNumber(roomDto.getRoomNumber())
                .building(roomDto.getBuilding())
                .capacity(roomDto.getCapacity())
                .build();

        return roomRepository.save(room);
    }

    @Transactional
    public Room updateRoom(Long id, RoomDto roomDto) {
        Room existingRoom = findById(id);

        existingRoom.setRoomNumber(roomDto.getRoomNumber());
        existingRoom.setBuilding(roomDto.getBuilding());
        existingRoom.setCapacity(roomDto.getCapacity());

        return roomRepository.save(existingRoom);
    }

    @Transactional
    public void deleteRoom(Long id) {
        if (!roomRepository.existsById(id)) {
            throw new IllegalArgumentException("Room with id '" + id + "' not found");
        }
        roomRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    /**
     * Create room and return DTO
     */
    @Transactional
    public RoomResponseDto createRoomDto(RoomDto roomDto) {
        Room room = createRoom(roomDto);
        return toDto(room);
    }

    /**
     * Update room and return DTO
     */
    @Transactional
    public RoomResponseDto updateRoomDto(Long id, RoomDto roomDto) {
        Room room = updateRoom(id, roomDto);
        return toDto(room);
    }

    /**
     * Get room by ID as DTO
     */
    @Transactional(readOnly = true)
    public RoomResponseDto findByIdDto(Long id) {
        Room room = findById(id);
        return toDto(room);
    }

    /**
     * Get all rooms as DTOs
     */
    @Transactional(readOnly = true)
    public List<RoomResponseDto> findAllDto() {
        return roomRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Map Room entity to RoomResponseDto
     */
    private RoomResponseDto toDto(Room room) {
        return RoomResponseDto.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .building(room.getBuilding())
                .capacity(room.getCapacity())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }
}
