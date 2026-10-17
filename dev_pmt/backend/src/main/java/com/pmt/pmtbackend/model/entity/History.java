package com.pmt.pmtbackend.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Représente une entrée de l'historique des modifications d'une tâche.
 * Elle conserve la trace du champ modifié, de l'ancienne et de la nouvelle valeur,
 * de l'auteur de la modification et la date et l'heure à laquelle elle a eu lieu.
 */
@Entity
@Table(name = "history")
public class History {

    /** Identifiant unique de l'entrée d'historique (généré par la base). */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nom du champ modifié (par exemple : "status", "priority"). */
    @NotBlank(message = "Le champ modifié est obligatoire")
    @Size(max = 100, message = "Le nom du champ ne peut pas dépasser 100 caractères")
    @Column(name = "modified_field", nullable = false)
    private String modifiedField;

    /** Ancienne valeur du champ (vide lors de la création d'une tâche). */
    @Size(max = 500, message = "L'ancienne valeur ne peut pas dépasser 500 caractères")
    @Column(name = "old_value")
    private String oldValue;

    /** Nouvelle valeur du champ (peut être vide si le champ a été effacé). */
    @Size(max = 500, message = "La nouvelle valeur ne peut pas dépasser 500 caractères")
    @Column(name = "new_value")
    private String newValue;

    /** Date et heure de la modification, renseignées automatiquement à l'enregistrement. */
    @CreationTimestamp
    @Column(name = "modified_date", updatable = false)
    private LocalDateTime modifiedDate;

    /** Tâche concernée par la modification (pas de cascade : on ne supprime jamais la tâche depuis l'historique). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    /** Utilisateur ayant effectué la modification (pas de cascade non plus). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by", nullable = false)
    private User modifiedBy;

    /** Constructeur vide requis par JPA. */
    public History() {
    }

    /**
     * Constructeur utilisé par les services pour tracer une modification.
     * La date est remplie automatiquement, on ne la passe donc pas ici.
     */
    public History(String modifiedField, String oldValue, String newValue, Task task, User modifiedBy) {
        this.modifiedField = modifiedField;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.task = task;
        this.modifiedBy = modifiedBy;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getModifiedField() { return modifiedField; }
    public void setModifiedField(String modifiedField) { this.modifiedField = modifiedField; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public LocalDateTime getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(LocalDateTime modifiedDate) { this.modifiedDate = modifiedDate; }

    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }

    public User getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(User modifiedBy) { this.modifiedBy = modifiedBy; }
}