package com.eoeqs.task_management_system.dtos;

import com.eoeqs.task_management_system.models.enums.TaskPriority;
import com.eoeqs.task_management_system.models.enums.TaskStatus;

import java.util.List;

public record TaskDTO(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        UserDTO author,
        UserDTO assignee,
        List<CommentDTO> comments
) {
}