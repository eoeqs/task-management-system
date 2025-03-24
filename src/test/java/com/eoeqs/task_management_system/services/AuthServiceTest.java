package com.eoeqs.task_management_system.services;

import com.eoeqs.task_management_system.dtos.AuthResponse;
import com.eoeqs.task_management_system.dtos.LoginRequest;
import com.eoeqs.task_management_system.dtos.RegisterRequest;
import com.eoeqs.task_management_system.models.User;
import com.eoeqs.task_management_system.models.enums.Role;
import com.eoeqs.task_management_system.repositories.UserRepository;
import com.eoeqs.task_management_system.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("test@example.com", "password", "Test User", Role.USER);
        loginRequest = new LoginRequest("test@example.com", "password");
    }

    @Test
    void register_success() {
        when(userRepository.findByEmail(registerRequest.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwtToken");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("jwtToken", response.token());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_emailAlreadyExists_throwsException() {
        when(userRepository.findByEmail(registerRequest.email())).thenReturn(Optional.of(new User()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.register(registerRequest));

        assertEquals("User with email test@example.com already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_success() {
        User user = new User();
        user.setEmail(loginRequest.email());
        user.setPassword("encodedPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwtToken");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwtToken", response.token());
    }

    @Test
    void login_invalidCredentials_throwsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new IllegalArgumentException("Invalid email or password"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authService.login(loginRequest));

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void register_adminRole_success() {
        RegisterRequest adminRequest = new RegisterRequest("admin@example.com", "adminPass", "Admin", Role.ADMIN);
        when(userRepository.findByEmail(adminRequest.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(adminRequest.password())).thenReturn("encodedAdminPass");
        when(jwtService.generateToken(any(User.class))).thenReturn("adminJwtToken");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.register(adminRequest);

        assertNotNull(response);
        assertEquals("adminJwtToken", response.token());
        verify(userRepository).save(argThat(user -> user.getRole() == Role.ADMIN));
    }
}