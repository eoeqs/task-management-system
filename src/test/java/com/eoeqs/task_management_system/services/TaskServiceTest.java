package com.eoeqs.task_management_system.services;

import com.eoeqs.task_management_system.dtos.TaskCreateRequest;
import com.eoeqs.task_management_system.dtos.TaskDTO;
import com.eoeqs.task_management_system.dtos.TaskUpdateRequest;
import com.eoeqs.task_management_system.models.Task;
import com.eoeqs.task_management_system.models.User;
import com.eoeqs.task_management_system.models.enums.Role;
import com.eoeqs.task_management_system.models.enums.TaskPriority;
import com.eoeqs.task_management_system.models.enums.TaskStatus;
import com.eoeqs.task_management_system.repositories.TaskRepository;
import com.eoeqs.task_management_system.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TaskService taskService;

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
        task.setTitle("Test Task");
        task.setAuthor(user);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createTask_success() {
        TaskCreateRequest request = new TaskCreateRequest("Test Task", "Description", TaskStatus.PENDING,
                TaskPriority.MEDIUM, null);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskDTO result = taskService.createTask(request);

        assertNotNull(result);
        assertEquals("Test Task", result.title());
        assertEquals(user.getId(), result.author().id());
    }

    @Test
    void updateTask_success() {
        TaskUpdateRequest request = new TaskUpdateRequest("Updated Task", null, TaskStatus.IN_PROGRESS, null, null);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskDTO result = taskService.updateTask(1L, request);

        assertEquals("Updated Task", result.title());
        assertEquals(TaskStatus.IN_PROGRESS, result.status());
    }

    @Test
    void deleteTask_success() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.deleteTask(1L);

        verify(taskRepository).delete(task);
    }

    @Test
    void getTasks_success() {
        Page<Task> page = new PageImpl<>(List.of(task));
        when(taskRepository.findTasksWithFilters(any(), any(), any(), any(), any())).thenReturn(page);

        Page<TaskDTO> result = taskService.getTasks(0, 10, null, null, null, null);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Task", result.getContent().get(0).title());
    }

    @Test
    void getTaskById_noPermission_throwsException() {
        Task unauthorizedTask = new Task();
        unauthorizedTask.setId(2L);
        unauthorizedTask.setAuthor(new User());

        when(taskRepository.findById(2L)).thenReturn(Optional.of(unauthorizedTask));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> taskService.getTaskById(2L));

        assertEquals("You do not have permission to view this task", exception.getMessage());
    }

    @Test
    void createTask_withAssignee_asAdmin_success() {
        User admin = new User();
        admin.setId(2L);
        admin.setEmail("admin@example.com");
        admin.setRole(Role.ADMIN);

        User assignee = new User();
        assignee.setId(3L);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(admin);
        SecurityContextHolder.setContext(securityContext);

        TaskCreateRequest request = new TaskCreateRequest("Admin Task", "Desc", TaskStatus.PENDING,
                TaskPriority.HIGH, 3L);
        when(userRepository.findById(3L)).thenReturn(Optional.of(assignee));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskDTO result = taskService.createTask(request);

        assertNotNull(result);
        assertEquals("Admin Task", result.title());
        assertEquals(3L, result.assignee().id());
    }

    @Test
    void createTask_withAssignee_asUser_throwsException() {
        TaskCreateRequest request = new TaskCreateRequest("User Task", "Desc", TaskStatus.PENDING,
                TaskPriority.MEDIUM, 2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(new User()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> taskService.createTask(request));

        assertEquals("Only admins can assign tasks to other users", exception.getMessage());
    }

    @Test
    void getTasks_withFilters_success() {
        Page<Task> page = new PageImpl<>(List.of(task));
        when(taskRepository.findTasksWithFilters(
                eq(TaskStatus.PENDING),
                eq(TaskPriority.MEDIUM),
                isNull(),
                isNull(),
                any(PageRequest.class)
        )).thenReturn(page);

        Page<TaskDTO> result = taskService.getTasks(0, 10, TaskStatus.PENDING, TaskPriority.MEDIUM, null, null);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Task", result.getContent().get(0).title());
    }

    @Test
    void updateTask_taskNotFound_throwsException() {
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> taskService.updateTask(999L, new TaskUpdateRequest("Updated", null, null, null, null)));

        assertEquals("Task not found", exception.getMessage());
    }
}