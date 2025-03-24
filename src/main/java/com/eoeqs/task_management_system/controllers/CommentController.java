package com.eoeqs.task_management_system.controllers;

import com.eoeqs.task_management_system.dtos.CommentCreateRequest;
import com.eoeqs.task_management_system.dtos.CommentDTO;
import com.eoeqs.task_management_system.services.CommentService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
@Tag(name = "Comments", description = "Endpoints for managing comments on tasks")
@SecurityRequirement(name = "bearerAuth")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @Operation(summary = "Create a comment", description = "Adds a new comment to a specific task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comment created successfully",
                    content = @Content(schema = @Schema(implementation = CommentDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "No permission to comment on this task",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Task not found",
                    content = @Content)
    })
    public ResponseEntity<CommentDTO> createComment(
            @Parameter(description = "ID of the task") @PathVariable Long taskId,
            @Valid @RequestBody CommentCreateRequest request
    ) {
        CommentDTO comment = commentService.createComment(taskId, request);
        return ResponseEntity.status(201).body(comment);
    }

    @GetMapping
    @Operation(summary = "Get comments for a task", description = "Retrieves all comments for a specific task")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comments retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CommentDTO.class))),
            @ApiResponse(responseCode = "403", description = "No permission to view comments",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Task not found",
                    content = @Content)
    })
    public ResponseEntity<List<CommentDTO>> getComments(
            @Parameter(description = "ID of the task") @PathVariable Long taskId
    ) {
        List<CommentDTO> comments = commentService.getCommentsByTaskId(taskId);
        return ResponseEntity.ok(comments);
    }
}