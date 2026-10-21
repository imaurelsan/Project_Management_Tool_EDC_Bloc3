package com.pmt.pmtbackend.dto;

import com.pmt.pmtbackend.model.entity.TaskPriority;
import com.pmt.pmtbackend.model.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO de création ou de modification d'une tâche.
 */
public record TaskRequest(
        @NotBlank(message = "Le titre est obligatoire")
        @Size(max = 150, message = "Le titre ne peut pas dépasser 150 caractères")
        String title,

        @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
        String description,

        @NotNull(message = "Le statut est obligatoire")
        TaskStatus status,

        @NotNull(message = "La priorité est obligatoire")
        TaskPriority priority,

        LocalDate dueDate,

        Long assigneeId) {
}