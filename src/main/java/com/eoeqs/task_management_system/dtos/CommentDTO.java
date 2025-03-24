package com.eoeqs.task_management_system.dtos;

import java.time.LocalDateTime;

public record CommentDTO(
        Long id,
        String text,
        UserDTO author,
        LocalDateTime createdAt
) {}