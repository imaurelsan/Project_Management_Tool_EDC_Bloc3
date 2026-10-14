package com.pmt.pmtbackend.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

/**
 * Représente l'appartenance d'un utilisateur à un projet.
 * Cette entité fait le lien entre un utilisateur et un projet,
 * et précise le rôle de l'utilisateur dans ce projet
 * (administrateur, membre ou observateur).
 * Un même utilisateur ne peut être associé qu'une seule fois à un même projet.
 */
@Entity
@Table(name = "project_member", uniqueConstraints = @UniqueConstraint(columnNames = { "project_id", "user_id" }))
public class ProjectMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Projet auquel appartient l'utilisateur.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * Utilisateur associé à ce projet.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Rôle de l'utilisateur dans le projet (administrateur, membre ou observateur).
     */
    @Enumerated(EnumType.STRING)
    @NotNull
    private ProjectRole role;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    public ProjectMember() {
    }

    /**
     * Constructeur pour initialiser un objet ProjectMember avec le projet,
     * l'utilisateur et le rôle.
     * 
     * @param project Projet auquel appartient l'utilisateur.
     * @param user    Utilisateur associé à ce projet.
     * @param role    Rôle de l'utilisateur dans le projet (administrateur, membre
     *                ou observateur).
     */
    public ProjectMember(Project project, User user, ProjectRole role) {
        this.project = project;
        this.user = user;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ProjectRole getRole() {
        return role;
    }

    public void setRole(ProjectRole role) {
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
