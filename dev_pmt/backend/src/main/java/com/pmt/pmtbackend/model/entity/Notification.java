package com.pmt.pmtbackend.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * Représente une notification envoyée à un utilisateur
 * suite à un événement lié à une tâche (par exemple, une assignation).
 */
@Entity
@Table(name = "notification")
public class Notification {

    /** Identifiant unique de la notification (généré par la base). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Contenu textuel de la notification. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * Indique si l'utilisateur a déjà lu la notification.
     * Nommé "is_read" en base car "read" est un mot réservé dans certains SGBD.
     */
    @Column(name = "is_read", nullable = false)
    private boolean read;

    /** Date de création de la notification, renseignée automatiquement. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** Destinataire de la notification. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Tâche à laquelle la notification est liée. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    /** Constructeur vide requis par JPA. */
    public Notification() {}

    /**
     * Constructeur pour créer une notification.
     *
     * @param message Contenu de la notification.
     * @param read    Statut de lecture initial.
     * @param user    Destinataire.
     * @param task    Tâche concernée.
     */
    public Notification(String message, boolean read, User user, Task task) {
        this.message = message;
        this.read = read;
        this.user = user;
        this.task = task;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
}