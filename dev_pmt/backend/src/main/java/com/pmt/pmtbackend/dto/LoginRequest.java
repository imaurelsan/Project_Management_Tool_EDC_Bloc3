package com.pmt.pmtbackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO pour la connexion d'un utilisateur.
 * Contient les informations nécessaires pour l'authentification.
 */
public record LoginRequest(
        @NotBlank(message = "L'adresse e-mail est obligatoire")
        @Email(message = "L'adresse e-mail doit être valide")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        String password) {
}