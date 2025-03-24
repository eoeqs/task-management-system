package com.eoeqs.task_management_system.repositories;

import com.eoeqs.task_management_system.models.Task;
import com.eoeqs.task_management_system.models.User;
import com.eoeqs.task_management_system.models.enums.TaskPriority;
import com.eoeqs.task_management_system.models.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t WHERE " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:priority IS NULL OR t.priority = :priority) AND " +
            "(:author IS NULL OR t.author = :author) AND " +
            "(:assignee IS NULL OR t.assignee = :assignee)")
    Page<Task> findTasksWithFilters(
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            @Param("author") User author,
            @Param("assignee") User assignee,
            Pageable pageable
    );
}