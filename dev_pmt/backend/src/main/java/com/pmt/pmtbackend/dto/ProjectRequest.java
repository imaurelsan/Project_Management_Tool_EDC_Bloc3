package com.pmt.pmtbackend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO pour la création ou la mise à jour d'un projet.
 * Contient les informations nécessaires pour créer un projet avec validation.
 */
public record ProjectRequest(
        @NotNull(message = "Le nom du projet est obligatoire")
        @Size(max = 100, message = "Le nom du projet ne doit pas dépasser 100 caractères")
        String name,

        @Size(max = 500, message = "La description ne doit pas dépasser 500 caractères")
        String description,

        @NotNull(message = "La date de début est obligatoire")
        String startDate) {
}