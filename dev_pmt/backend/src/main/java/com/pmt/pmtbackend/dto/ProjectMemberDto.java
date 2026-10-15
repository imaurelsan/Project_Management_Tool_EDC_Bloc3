package com.pmt.pmtbackend.dto;

import com.pmt.pmtbackend.model.entity.ProjectRole;

/**
 * DTO pour représenter un membre d'un projet.
 * Contient les informations de l'utilisateur et son rôle dans le projet.
 */
public record ProjectMemberDto(Long userId, String username, String email, ProjectRole role) {
}