package com.pmt.pmtbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import com.pmt.pmtbackend.model.entity.ProjectRole;

/**
 * DTO pour l'ajout d'un membre à un projet.
 * Contient l'e-mail de l'utilisateur et le rôle à attribuer.
 */
public record AddMemberRequest(
        @NotNull(message = "L'e-mail est obligatoire")
        @Email(message = "L'e-mail doit être valide")
        String email,

        @NotNull(message = "Le rôle est obligatoire")
        ProjectRole role) {
}