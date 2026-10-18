package com.pmt.pmtbackend.dto;

import com.pmt.pmtbackend.model.entity.ProjectRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO pour l'ajout d'un membre à un projet.
 * Contient l'e-mail de l'utilisateur et le rôle à attribuer.
 */
public record AddMemberRequest(
        @NotBlank(message = "L'e-mail est obligatoire")
        @Email(message = "L'e-mail doit être valide")
        String email,

        @NotNull(message = "Le rôle est obligatoire")
        ProjectRole role) {
}