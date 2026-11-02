package com.pmt.pmtbackend.dto;

import java.time.LocalDateTime;

/**
 * Données d'une notification affichées par le frontend.
 *
 * @param id identifiant de la notification
 * @param message texte à afficher
 * @param read indique si elle a été lue
 * @param createdAt date de création
 * @param taskId identifiant de la tâche liée
 * @param projectId identifiant du projet lié
 * @param userId identifiant du destinataire
 */
public record NotificationDto(Long id, String message, boolean read,
                              LocalDateTime createdAt, Long taskId, Long projectId, Long userId) {
}
