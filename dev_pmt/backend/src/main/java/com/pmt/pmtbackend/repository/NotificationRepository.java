package com.pmt.pmtbackend.repository;

import com.pmt.pmtbackend.model.entity.Notification;
import com.pmt.pmtbackend.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository pour l'entité Notification.
 * Permet de gérer les notifications des utilisateurs.
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Notifications d'un utilisateur, de la plus récente à la plus ancienne.
     * "User" correspond au champ "user" de l'entité Notification.
     */
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Retourne les notifications d'un utilisateur par identifiant.
     *
     * @param userId identifiant du destinataire
     * @return notifications triées par date décroissante
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Recherche une notification en vérifiant directement son destinataire.
     *
     * @param id identifiant de la notification
     * @param userId identifiant du destinataire
     * @return notification appartenant à l'utilisateur si elle existe
     */
    java.util.Optional<Notification> findByIdAndUserId(Long id, Long userId);
}