package com.pmt.pmtbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pour la création ou la mise à jour d'un projet.
 * Les champs correspondent exactement à la table project (pas de date de début pour l'instant).
 */
public record ProjectRequest(
        @NotBlank(message = "Le nom du projet est obligatoire")
        @Size(max = 100, message = "Le nom du projet ne doit pas dépasser 100 caractères")
        String name,

        @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
        String description) {
}