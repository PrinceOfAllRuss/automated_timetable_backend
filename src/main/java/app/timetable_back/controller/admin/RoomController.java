package app.timetable_back.controller.admin;

import app.timetable_back.dto.RoomDto;
import app.timetable_back.dto.RoomResponseDto;
import app.timetable_back.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Room Management", description = "API для управления аудиториями")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping("/create-room")
    @Operation(summary = "Create new room", description = "Создание новой аудитории")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Room created successfully",
                    content = @Content(schema = @Schema(implementation = RoomResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<RoomResponseDto> createRoom(@Valid @RequestBody RoomDto roomDto) {
        RoomResponseDto createdRoom = roomService.createRoomDto(roomDto);
        return ResponseEntity.created(URI.create("/admin/rooms/" + createdRoom.getId())).body(createdRoom);
    }

    @PutMapping("/update-room/{roomId}")
    @Operation(summary = "Update room", description = "Обновление данных аудитории")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Room updated successfully",
                    content = @Content(schema = @Schema(implementation = RoomResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Room not found")
    })
    public ResponseEntity<RoomResponseDto> updateRoom(
            @Parameter(description = "Room ID", required = true) @PathVariable Long roomId,
            @Valid @RequestBody RoomDto roomDto) {
        RoomResponseDto updatedRoom = roomService.updateRoomDto(roomId, roomDto);
        return ResponseEntity.ok(updatedRoom);
    }

    @DeleteMapping("/delete-room/{roomId}")
    @Operation(summary = "Delete room", description = "Удаление аудитории")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Room deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Room not found")
    })
    public ResponseEntity<Void> deleteRoom(
            @Parameter(description = "Room ID", required = true) @PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/room/{roomId}")
    @Operation(summary = "Get room by ID", description = "Получение данных аудитории по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Room found",
                    content = @Content(schema = @Schema(implementation = RoomResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Room not found")
    })
    public ResponseEntity<RoomResponseDto> getRoom(
            @Parameter(description = "Room ID", required = true) @PathVariable Long roomId) {
        RoomResponseDto room = roomService.findByIdDto(roomId);
        return ResponseEntity.ok(room);
    }

    @GetMapping("/rooms")
    @Operation(summary = "Get all rooms", description = "Получение списка всех аудиторий")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rooms found",
                    content = @Content(schema = @Schema(implementation = RoomResponseDto.class)))
    })
    public ResponseEntity<List<RoomResponseDto>> getAllRooms() {
        List<RoomResponseDto> rooms = roomService.findAllDto();
        return ResponseEntity.ok(rooms);
    }
}
