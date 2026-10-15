package com.pmt.pmtbackend.dto;

import jakarta.validation.constraints.NotNull;
import com.pmt.pmtbackend.model.entity.ProjectRole;

/**
 * DTO pour la mise à jour du rôle d'un membre dans un projet.
 * Contient uniquement le nouveau rôle à attribuer.
 */
public record UpdateMemberRoleRequest(
        @NotNull(message = "Le rôle est obligatoire")
        ProjectRole role) {
}