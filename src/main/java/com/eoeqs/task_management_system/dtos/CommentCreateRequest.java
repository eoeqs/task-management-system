package com.eoeqs.task_management_system.dtos;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequest(
        @NotBlank(message = "Comment text cannot be blank") String text
) {
}