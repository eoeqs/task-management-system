package com.eoeqs.task_management_system.dtos;

import com.eoeqs.task_management_system.models.enums.TaskPriority;
import com.eoeqs.task_management_system.models.enums.TaskStatus;
import jakarta.validation.constraints.*;

public record TaskCreateRequest(
        @NotBlank(message = "Title cannot be blank")
        @Size(max = 100, message = "Title cannot exceed 100 characters") String title,
        @Size(max = 500, message = "Description cannot exceed 500 characters") String description,
        @NotNull(message = "Status cannot be null") TaskStatus status,
        @NotNull(message = "Priority cannot be null") TaskPriority priority,
        Long assigneeId
) {}