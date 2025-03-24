package com.eoeqs.task_management_system.dtos;

import jakarta.validation.constraints.*;

public record LoginRequest(
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format") String email,
        @NotBlank(message = "Password cannot be blank") String password
) {}