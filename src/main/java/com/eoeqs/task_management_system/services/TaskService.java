package com.eoeqs.task_management_system.services;

import com.eoeqs.task_management_system.dtos.*;
import com.eoeqs.task_management_system.models.Task;
import com.eoeqs.task_management_system.models.User;
import com.eoeqs.task_management_system.models.Comment;
import com.eoeqs.task_management_system.models.enums.Role;
import com.eoeqs.task_management_system.models.enums.TaskPriority;
import com.eoeqs.task_management_system.models.enums.TaskStatus;
import com.eoeqs.task_management_system.repositories.TaskRepository;
import com.eoeqs.task_management_system.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public TaskDTO createTask(TaskCreateRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setAuthor(currentUser);

        if (request.assigneeId() != null) {
            User assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new IllegalArgumentException("Assignee not found"));
            if (!currentUser.getRole().equals(Role.ADMIN) && !currentUser.getId().equals(assignee.getId())) {
                throw new IllegalArgumentException("Only admins can assign tasks to other users");
            }
            task.setAssignee(assignee);
        }

        taskRepository.save(task);
        return mapToTaskDTO(task);
    }

    public TaskDTO updateTask(Long taskId, TaskUpdateRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (!hasAccessToTask(currentUser, task)) {
            throw new IllegalArgumentException("You do not have permission to update this task");
        }

        if (request.title() != null) task.setTitle(request.title());
        if (request.description() != null) task.setDescription(request.description());
        if (request.status() != null) task.setStatus(request.status());
        if (request.priority() != null) task.setPriority(request.priority());
        if (request.assigneeId() != null) {
            User assignee = userRepository.findById(request.assigneeId())
                    .orElseThrow(() -> new IllegalArgumentException("Assignee not found"));
            if (!currentUser.getRole().equals(Role.ADMIN)) {
                throw new IllegalArgumentException("Only admins can reassign tasks");
            }
            task.setAssignee(assignee);
        }

        taskRepository.save(task);
        return mapToTaskDTO(task);
    }

    public void deleteTask(Long taskId) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (!hasAccessToTask(currentUser, task)) {
            throw new IllegalArgumentException("You do not have permission to delete this task");
        }

        taskRepository.delete(task);
    }

    public Page<TaskDTO> getTasks(
            int page,
            int size,
            TaskStatus status,
            TaskPriority priority,
            Long authorId,
            Long assigneeId
    ) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Pageable pageable = PageRequest.of(page, size);

        User author = authorId != null ? userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found")) : null;
        User assignee = assigneeId != null ? userRepository.findById(assigneeId)
                .orElseThrow(() -> new IllegalArgumentException("Assignee not found")) : null;

        if (currentUser.getRole().equals(Role.ADMIN)) {
            // Администратор видит все задачи с учетом фильтров
            return taskRepository.findTasksWithFilters(status, priority, author, assignee, pageable)
                    .map(this::mapToTaskDTO);
        } else {
            // Обычный пользователь видит только свои задачи
            return taskRepository.findTasksWithFilters(
                    status,
                    priority,
                    author != null && author.getId().equals(currentUser.getId()) ? currentUser : null,
                    assignee != null && assignee.getId().equals(currentUser.getId()) ? currentUser : null,
                    pageable
            ).map(this::mapToTaskDTO);
        }
    }

    public TaskDTO getTaskById(Long taskId) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (!hasAccessToTask(currentUser, task)) {
            throw new IllegalArgumentException("You do not have permission to view this task");
        }

        return mapToTaskDTO(task);
    }

    private boolean hasAccessToTask(User user, Task task) {
        return user.getRole().equals(Role.ADMIN) ||
                task.getAuthor().getId().equals(user.getId()) ||
                (task.getAssignee() != null && task.getAssignee().getId().equals(user.getId()));
    }

    private TaskDTO mapToTaskDTO(Task task) {
        return new TaskDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                mapToUserDTO(task.getAuthor()),
                task.getAssignee() != null ? mapToUserDTO(task.getAssignee()) : null,
                task.getComments().stream().map(this::mapToCommentDTO).collect(Collectors.toList())
        );
    }

    private UserDTO mapToUserDTO(User user) {
        return new UserDTO(user.getId(), user.getEmail(), user.getName(), user.getRole());
    }

    private CommentDTO mapToCommentDTO(Comment comment) {
        return new CommentDTO(
                comment.getId(),
                comment.getText(),
                mapToUserDTO(comment.getAuthor()),
                comment.getCreatedAt()
        );
    }
}