package com.eoeqs.task_management_system.repositories;

import com.eoeqs.task_management_system.models.Comment;
import com.eoeqs.task_management_system.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTask(Task task);
}