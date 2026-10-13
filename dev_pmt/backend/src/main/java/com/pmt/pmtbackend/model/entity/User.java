package com.pmt.pmtbackend.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Représente un utilisateur de l'application.
 * Un utilisateur peut créer des projets, être membre de projets,
 * se voir assigner des tâches et recevoir des notifications.
 */
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nom d'utilisateur unique, utilisé pour l'authentification.
     */
    @Column(unique = true, nullable = false)
    @NotBlank(message = "Le nom d'utilisateur est obligatoire.")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit avoir entre 3 et 50 caractères.")
    private String username;

    /**
     * Adresse e-mail unique associée à l'utilisateur.
     */
    @Column(unique = true, nullable = false)
    @NotBlank(message = "L'e-mail est obligatoire.")
    @Email(message = "L'e-mail doit être valide.")
    private String email;

    /**
     * Mot de passe de l'utilisateur (haché).
     */
    @Column(nullable = false)
    @NotBlank(message = "Le mot de passe est obligatoire.")
    @Size(min = 8, message = "Le mot de passe doit avoir au moins 8 caractères.")
    private String password;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<Project> ownedProjects = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<ProjectMember> projectMemberships = new ArrayList<>();

    @OneToMany(mappedBy = "assignee", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<Task> assignedTasks = new ArrayList<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "modifiedBy", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<History> histories = new ArrayList<>();

    public User() {}

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<Project> getOwnedProjects() { return ownedProjects; }
    public void setOwnedProjects(List<Project> ownedProjects) { this.ownedProjects = ownedProjects; }
    public List<ProjectMember> getProjectMemberships() { return projectMemberships; }
    public void setProjectMemberships(List<ProjectMember> projectMemberships) { this.projectMemberships = projectMemberships; }
    public List<Task> getAssignedTasks() { return assignedTasks; }
    public void setAssignedTasks(List<Task> assignedTasks) { this.assignedTasks = assignedTasks; }
    public List<Notification> getNotifications() { return notifications; }
    public void setNotifications(List<Notification> notifications) { this.notifications = notifications; }
    public List<History> getHistories() { return histories; }
    public void setHistories(List<History> histories) { this.histories = histories; }
}

