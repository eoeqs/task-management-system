package com.eoeqs.task_management_system.dtos;

import com.eoeqs.task_management_system.models.enums.Role;

public record UserDTO(
        Long id,
        String email,
        String name,
        Role role
) {
}