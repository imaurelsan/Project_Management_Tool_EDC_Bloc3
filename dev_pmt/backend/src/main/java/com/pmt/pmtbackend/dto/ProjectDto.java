package com.pmt.pmtbackend.dto;

import com.pmt.pmtbackend.model.entity.ProjectRole;

/**
 * DTO pour représenter un projet.
 * Contient les informations principales d'un projet sans détails sensibles.
 */
public record ProjectDto(Long id, String name, String description, String startDate, Long creatorId, String creatorUsername) {
}