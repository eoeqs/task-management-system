package com.eoeqs.task_management_system.dtos;

import com.eoeqs.task_management_system.models.enums.TaskPriority;
import com.eoeqs.task_management_system.models.enums.TaskStatus;
import jakarta.validation.constraints.Size;

public record TaskUpdateRequest(
        @Size(max = 100, message = "Title cannot exceed 100 characters") String title,
        @Size(max = 500, message = "Description cannot exceed 500 characters") String description,
        TaskStatus status,
        TaskPriority priority,
        Long assigneeId
) {
}