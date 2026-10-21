package com.pmt.pmtbackend.dto;

import com.pmt.pmtbackend.model.entity.TaskPriority;
import com.pmt.pmtbackend.model.entity.TaskStatus;

import java.time.LocalDate;

/**
 * DTO de lecture d'une tâche.
 */
public record TaskDto(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        Long projectId,
        Long assigneeId,
        String assigneeUsername,
        String createdAt,
        String updatedAt) {
}