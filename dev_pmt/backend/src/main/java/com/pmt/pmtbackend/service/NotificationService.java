package com.pmt.pmtbackend.service;

import com.pmt.pmtbackend.model.entity.Notification;
import com.pmt.pmtbackend.model.entity.Task;
import com.pmt.pmtbackend.model.entity.User;
import com.pmt.pmtbackend.dto.NotificationDto;
import com.pmt.pmtbackend.exception.ForbiddenException;
import com.pmt.pmtbackend.exception.ResourceNotFoundException;
import com.pmt.pmtbackend.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service chargé d'informer un utilisateur qu'une tâche lui a été assignée.
 * Enregistre la notification en base puis envoie un e-mail.
 *
 * Un échec d'envoi d'e-mail ne doit pas faire échouer l'opération métier :
 * l'erreur est journalisée et la notification reste enregistrée.
 */
@Service
public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;
    private final String fromAddress;

    /**
     * Construit le service de notification.
     *
     * @param notificationRepository  dépôt des notifications
     * @param mailSender              composant d'envoi d'e-mails
     * @param fromAddress             adresse utilisée comme expéditeur
     */
    public NotificationService(NotificationRepository notificationRepository,
                               JavaMailSender mailSender,
                               @Value("${notification.mail.from}") String fromAddress) {
        this.notificationRepository = notificationRepository;
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    /**
     * Notifie le nouvel assigné d'une tâche.
     *
     * @param assignee le membre à qui la tâche est assignée
     * @param task      la tâche concernée
     * @return aucune valeur ; l'e-mail est seulement tenté après l'enregistrement
     */
    public void notifyAssignment(User assignee, Task task) {
        String message = "La tâche « " + task.getTitle() + " » vous a été assignée.";
        notificationRepository.save(new Notification(assignee, task, message));

        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromAddress);
            mail.setTo(assignee.getEmail());
            mail.setSubject("PMT : nouvelle tâche assignée");
            mail.setText(message + "\n\nProjet : " + task.getProject().getName());
            mailSender.send(mail);
        } catch (Exception e) {
            // Ne pas bloquer la mise à jour de la tâche si le serveur mail est indisponible.
            LOGGER.warn("Échec de l'envoi de l'e-mail à {}", assignee.getEmail(), e);
        }
    }

    /**
     * Liste les notifications personnelles, de la plus récente à la plus ancienne.
     *
     * @param userId identifiant du destinataire
     * @return liste des notifications converties en DTO
     */
    @Transactional(readOnly = true)
    public List<NotificationDto> listForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Marque une notification appartenant à l'utilisateur comme lue.
     *
     * @param userId identifiant du destinataire
     * @param notificationId identifiant de la notification
     */
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable."));
        if (!notification.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Cette notification ne vous appartient pas.");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    /**
     * Convertit une notification persistée en objet exposé par l'API.
     *
     * @param notification notification à convertir
     * @return notification prête à être sérialisée
     */
    private NotificationDto toDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getTask().getId(),
                notification.getTask().getProject().getId(),
                notification.getUser().getId());
    }
}