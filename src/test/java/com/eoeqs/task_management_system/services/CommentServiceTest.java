package com.eoeqs.task_management_system.services;

import com.eoeqs.task_management_system.dtos.CommentCreateRequest;
import com.eoeqs.task_management_system.dtos.CommentDTO;
import com.eoeqs.task_management_system.models.*;
import com.eoeqs.task_management_system.models.enums.Role;
import com.eoeqs.task_management_system.repositories.CommentRepository;
import com.eoeqs.task_management_system.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CommentService commentService;

    private User user;
    private Task task;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole(Role.USER);

        task = new Task();
        task.setId(1L);
        task.setAuthor(user);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createComment_success() {
        CommentCreateRequest request = new CommentCreateRequest("Test comment");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommentDTO result = commentService.createComment(1L, request);

        assertNotNull(result);
        assertEquals("Test comment", result.text());
        assertEquals(user.getId(), result.author().id());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_noPermission_throwsException() {
        Task unauthorizedTask = new Task();
        unauthorizedTask.setId(2L);
        unauthorizedTask.setAuthor(new User());
        when(taskRepository.findById(2L)).thenReturn(Optional.of(unauthorizedTask));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> commentService.createComment(2L, new CommentCreateRequest("Test")));

        assertEquals("You do not have permission to comment on this task", exception.getMessage());
    }

    @Test
    void getCommentsByTaskId_success() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Test comment");
        comment.setAuthor(user);
        comment.setCreatedAt(LocalDateTime.now());

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(commentRepository.findByTask(task)).thenReturn(List.of(comment));

        List<CommentDTO> result = commentService.getCommentsByTaskId(1L);

        assertEquals(1, result.size());
        assertEquals("Test comment", result.get(0).text());
    }

    @Test
    void createComment_asAdmin_success() {
        User admin = new User();
        admin.setId(2L);
        admin.setEmail("admin@example.com");
        admin.setRole(Role.ADMIN);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(admin);
        SecurityContextHolder.setContext(securityContext);

        Task otherTask = new Task();
        otherTask.setId(3L);
        otherTask.setAuthor(new User());

        CommentCreateRequest request = new CommentCreateRequest("Admin comment");
        when(taskRepository.findById(3L)).thenReturn(Optional.of(otherTask));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommentDTO result = commentService.createComment(3L, request);

        assertNotNull(result);
        assertEquals("Admin comment", result.text());
        assertEquals(admin.getId(), result.author().id());
    }

    @Test
    void createComment_taskNotFound_throwsException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> commentService.createComment(999L, new CommentCreateRequest("Test")));

        assertEquals("Task not found", exception.getMessage());
    }
}