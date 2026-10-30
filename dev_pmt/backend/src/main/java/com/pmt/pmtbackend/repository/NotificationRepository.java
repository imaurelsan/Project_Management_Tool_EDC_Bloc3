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
}