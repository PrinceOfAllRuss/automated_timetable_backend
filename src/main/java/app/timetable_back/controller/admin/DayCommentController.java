package app.timetable_back.controller.admin;

import app.timetable_back.dto.DayCommentDto;
import app.timetable_back.dto.DayCommentResponseDto;
import app.timetable_back.service.DayCommentService;
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
@Tag(name = "Day Comment Management", description = "API для управления комментариями к дням")
public class DayCommentController {

    private final DayCommentService dayCommentService;

    public DayCommentController(DayCommentService dayCommentService) {
        this.dayCommentService = dayCommentService;
    }

    @PostMapping("/create-day-comment")
    @Operation(summary = "Create new day comment", description = "Создание нового комментария к дню")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Day comment created successfully",
                    content = @Content(schema = @Schema(implementation = DayCommentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<DayCommentResponseDto> createDayComment(@Valid @RequestBody DayCommentDto dayCommentDto) {
        DayCommentResponseDto createdDayComment = dayCommentService.createDayCommentDto(dayCommentDto);
        return ResponseEntity.created(URI.create("/admin/day-comments/" + createdDayComment.getId())).body(createdDayComment);
    }

    @PutMapping("/update-day-comment/{commentId}")
    @Operation(summary = "Update day comment", description = "Обновление комментария к дню")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Day comment updated successfully",
                    content = @Content(schema = @Schema(implementation = DayCommentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Day comment or User not found")
    })
    public ResponseEntity<DayCommentResponseDto> updateDayComment(
            @Parameter(description = "Day Comment ID", required = true) @PathVariable Long commentId,
            @Valid @RequestBody DayCommentDto dayCommentDto) {
        DayCommentResponseDto updatedDayComment = dayCommentService.updateDayCommentDto(commentId, dayCommentDto);
        return ResponseEntity.ok(updatedDayComment);
    }

    @DeleteMapping("/delete-day-comment/{commentId}")
    @Operation(summary = "Delete day comment", description = "Удаление комментария к дню")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Day comment deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Day comment not found")
    })
    public ResponseEntity<Void> deleteDayComment(
            @Parameter(description = "Day Comment ID", required = true) @PathVariable Long commentId) {
        dayCommentService.deleteDayComment(commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/day-comment/{commentId}")
    @Operation(summary = "Get day comment by ID", description = "Получение комментария по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Day comment found",
                    content = @Content(schema = @Schema(implementation = DayCommentResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Day comment not found")
    })
    public ResponseEntity<DayCommentResponseDto> getDayComment(
            @Parameter(description = "Day Comment ID", required = true) @PathVariable Long commentId) {
        DayCommentResponseDto dayComment = dayCommentService.findByIdDto(commentId);
        return ResponseEntity.ok(dayComment);
    }

    @GetMapping("/day-comments")
    @Operation(summary = "Get all day comments", description = "Получение списка всех комментариев")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Day comments found",
                    content = @Content(schema = @Schema(implementation = DayCommentResponseDto.class)))
    })
    public ResponseEntity<List<DayCommentResponseDto>> getAllDayComments() {
        List<DayCommentResponseDto> dayComments = dayCommentService.findAllDto();
        return ResponseEntity.ok(dayComments);
    }
}
