package com.pmt.pmtbackend.dto;

import com.pmt.pmtbackend.model.entity.ProjectRole;

import java.io.Serializable;

/**
 * DTO pour représenter un utilisateur sans inclure le mot de passe.
 * Utilisé principalement lors des opérations de lecture ou d'affichage.
 */
public record UserDto(Long id, String username, String email) implements Serializable {
}