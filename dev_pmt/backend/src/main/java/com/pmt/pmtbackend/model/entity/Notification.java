package com.pmt.pmtbackend.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * Représente une notification envoyée à un utilisateur,
 * par exemple lorsqu'une tâche lui est assignée.
 * Elle indique si elle a déjà été lue ou non.
 */
@Entity
@Table(name = "notifications")
public class Notification {

    /** Identifiant unique de la notification (généré automatiquement). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Message affiché à l'utilisateur. */
    @NotBlank(message = "Le message est obligatoire")
    @Size(max = 500, message = "Le message ne peut pas dépasser 500 caractères")
    @Column(nullable = false, length = 500)
    private String message;

    /**
     * Indique si la notification a été lue.
     * Colonne nommée "is_read" car "read" est un mot réservé en SQL.
     */
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    /** Date de création, renseignée automatiquement par Hibernate. */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Utilisateur destinataire de la notification. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Tâche concernée par la notification. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    /** Constructeur vide requis par JPA. */
    public Notification() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { this.isRead = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
}