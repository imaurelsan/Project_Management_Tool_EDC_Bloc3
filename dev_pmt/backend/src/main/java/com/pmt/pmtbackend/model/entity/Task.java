package com.pmt.pmtbackend.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente une tâche à réaliser au sein d'un projet.
 * Une tâche appartient à un projet, peut être assignée à un utilisateur,
 * possède un statut (à faire, en cours, fait, annulé)
 * et un niveau de priorité (faible, moyen, haute, critique).
 */
@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Titre de la tâche.
     */
    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 150, message = "Le titre ne peut pas dépasser 150 caractères")
    @Column(nullable = false, length = 150)
    private String title;

    /**
     * Description de la tâche.
     */
    @Size(max = 2000, message = "La description ne peut pas dépasser 2000 caractères")
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Statut de la tâche (à faire, en cours, fait, annulé).
     */
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Le statut est obligatoire")
    @Column(nullable = false)
    private TaskStatus status;

    /**
     * Priorité de la tâche (faible, moyen, haute, critique).
     */
    @Enumerated(EnumType.STRING)
    @NotNull(message = "La priorité est obligatoire")
    @Column(nullable = false)
    private TaskPriority priority;

    /**
     * Date d'échéance de la tâche.
     */
    @Column(name = "due_date")
    private LocalDate dueDate;

    /**
     * Date de création de la tâche.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Date de dernière modification de la tâche.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Projet auquel appartient cette tâche.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * Utilisateur assigné à cette tâche.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    /**
     * Liste des notifications liées à cette tâche.
     */
    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST,
            CascadeType.MERGE }, orphanRemoval = true)
    private List<Notification> notifications = new ArrayList<>();

    /**
     * Historique des modifications de cette tâche.
     */
    @OneToMany(mappedBy = "task", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST,
            CascadeType.MERGE }, orphanRemoval = true)
    private List<History> histories = new ArrayList<>();

    /**
     * Constructeur par défaut.
     */
    public Task() {
    }

    /**
     * Constructeur pour initialiser un objet Task avec les paramètres essentiels.
     * 
     * @param title       Titre de la tâche.
     * @param description Description de la tâche.
     * @param status      Statut de la tâche (à faire, en cours, fait, annulé).
     * @param priority    Priorité de la tâche (faible, moyen, haute, critique).
     * @param dueDate     Date d'échéance de la tâche.
     * @param project     Projet auquel appartient la tâche.
     * @param assignee    Utilisateur assigné à la tâche.
     */
    public Task(String title, String description, TaskStatus status, TaskPriority priority, LocalDate dueDate,
            Project project, User assignee) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.project = project;
        this.assignee = assignee;
    }

    // Getters et Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public List<History> getHistories() {
        return histories;
    }

    public void setHistories(List<History> histories) {
        this.histories = histories;
    }
}