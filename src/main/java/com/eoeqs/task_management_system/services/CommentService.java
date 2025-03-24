package com.eoeqs.task_management_system.services;

import com.eoeqs.task_management_system.dtos.CommentCreateRequest;
import com.eoeqs.task_management_system.dtos.CommentDTO;
import com.eoeqs.task_management_system.dtos.UserDTO;
import com.eoeqs.task_management_system.models.*;
import com.eoeqs.task_management_system.models.enums.Role;
import com.eoeqs.task_management_system.repositories.CommentRepository;
import com.eoeqs.task_management_system.repositories.TaskRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;

    public CommentService(CommentRepository commentRepository, TaskRepository taskRepository) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
    }

    public CommentDTO createComment(Long taskId, CommentCreateRequest request) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (!hasAccessToTask(currentUser, task)) {
            throw new IllegalArgumentException("You do not have permission to comment on this task");
        }

        Comment comment = new Comment();
        comment.setText(request.text());
        comment.setAuthor(currentUser);
        comment.setTask(task);

        commentRepository.save(comment);
        return mapToCommentDTO(comment);
    }

    public List<CommentDTO> getCommentsByTaskId(Long taskId) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        if (!hasAccessToTask(currentUser, task)) {
            throw new IllegalArgumentException("You do not have permission to view comments for this task");
        }

        return commentRepository.findByTask(task).stream()
                .map(this::mapToCommentDTO)
                .collect(Collectors.toList());
    }

    private boolean hasAccessToTask(User user, Task task) {
        return user.getRole().equals(Role.ADMIN) ||
                task.getAuthor().getId().equals(user.getId()) ||
                (task.getAssignee() != null && task.getAssignee().getId().equals(user.getId()));
    }

    private CommentDTO mapToCommentDTO(Comment comment) {
        return new CommentDTO(
                comment.getId(),
                comment.getText(),
                mapToUserDTO(comment.getAuthor()),
                comment.getCreatedAt()
        );
    }

    private UserDTO mapToUserDTO(User user) {
        return new UserDTO(user.getId(), user.getEmail(), user.getName(), user.getRole());
    }
}