package com.pmt.pmtbackend.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Représente une notification adressée à un utilisateur à propos d'une tâche
 * (par exemple lors d'une assignation).
 * Table : notification.
 */
@Entity
@Table(name = "notification")
public class Notification {

    /** Identifiant technique (auto-incrémenté). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Destinataire de la notification. Le nom "user" doit correspondre au mappedBy de User.notifications. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Tâche concernée par la notification. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    /** Texte affiché au destinataire. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /** Indique si la notification a été lue (colonne is_read, car read est un mot réservé SQL). */
    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    /** Date de création, renseignée automatiquement avant l'insertion. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Renseigne la date de création avant l'insertion en base. */
    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /** Construit une notification vide pour Hibernate. */
    public Notification() {
    }

    /**
     * Construit une notification destinée à un utilisateur pour une tâche.
     *
     * @param user    destinataire de la notification
     * @param task    tâche concernée
     * @param message texte de la notification
     */
    public Notification(User user, Task task, String message) {
        this.user = user;
        this.task = task;
        this.message = message;
    }

    /**
     * Retourne l'identifiant de la notification.
     *
     * @return identifiant de la notification
     */
    public Long getId() { return id; }

    /**
     * Retourne le destinataire de la notification.
     *
     * @return utilisateur destinataire
     */
    public User getUser() { return user; }

    /**
     * Modifie le destinataire de la notification.
     *
     * @param user nouveau destinataire
     */
    public void setUser(User user) { this.user = user; }

    /**
     * Retourne la tâche concernée.
     *
     * @return tâche concernée
     */
    public Task getTask() { return task; }

    /**
     * Modifie la tâche concernée.
     *
     * @param task nouvelle tâche concernée
     */
    public void setTask(Task task) { this.task = task; }

    /**
     * Retourne le texte de la notification.
     *
     * @return message de la notification
     */
    public String getMessage() { return message; }

    /**
     * Modifie le texte de la notification.
     *
     * @param message nouveau message
     */
    public void setMessage(String message) { this.message = message; }

    /**
     * Indique si la notification a été lue.
     *
     * @return true si la notification est lue, sinon false
     */
    public boolean isRead() { return read; }

    /**
     * Modifie l'état de lecture de la notification.
     *
     * @param read nouvel état de lecture
     */
    public void setRead(boolean read) { this.read = read; }

    /**
     * Retourne la date de création.
     *
     * @return date de création
     */
    public LocalDateTime getCreatedAt() { return createdAt; }
}