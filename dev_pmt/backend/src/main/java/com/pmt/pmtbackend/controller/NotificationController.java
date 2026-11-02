package com.pmt.pmtbackend.controller;

import com.pmt.pmtbackend.dto.NotificationDto;
import com.pmt.pmtbackend.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur de consultation et de lecture des notifications personnelles.
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Construit le contrôleur des notifications.
     *
     * @param notificationService service de notification
     */
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * Retourne les notifications du compte courant.
     *
     * @param userId identifiant du compte courant
     * @return notifications de la plus récente à la plus ancienne
     */
    @GetMapping
    public List<NotificationDto> list(@RequestHeader("X-User-Id") Long userId) {
        return notificationService.listForUser(userId);
    }

    /**
     * Marque une notification appartenant au compte courant comme lue.
     *
     * @param userId identifiant du compte courant
     * @param notificationId identifiant de la notification
     * @return réponse vide avec le statut 204
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@RequestHeader("X-User-Id") Long userId,
                                           @PathVariable Long notificationId) {
        notificationService.markAsRead(userId, notificationId);
        return ResponseEntity.noContent().build();
    }
}
