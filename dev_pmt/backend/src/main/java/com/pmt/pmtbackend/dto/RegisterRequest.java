package com.pmt.pmtbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pour la création d'un utilisateur.
 * Contient les informations nécessaires pour l'enregistrement d'un nouvel utilisateur.
 */
public record RegisterRequest(
        @NotBlank(message = "Le nom d'utilisateur est obligatoire")
        @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit avoir entre 3 et 50 caractères")
        String username,

        @NotBlank(message = "L'adresse e-mail est obligatoire")
        @Email(message = "L'adresse e-mail doit être valide")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
        String password) {
}