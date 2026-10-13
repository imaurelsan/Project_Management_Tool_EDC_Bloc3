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
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Récupère toutes les notifications d'un utilisateur, triées par date décroissante (plus récentes en premier).
     * @param user l'utilisateur destinataire
     * @return liste des notifications ordonnées par date (descendante)
     */
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Récupère les notifications non-lues d'un utilisateur.
     * @param user l'utilisateur
     * @return liste des notifications non-lues
     */
    List<Notification> findByUserAndIsReadFalse(User user);
}