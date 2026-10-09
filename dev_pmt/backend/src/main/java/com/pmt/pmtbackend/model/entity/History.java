package com.pmt.pmtbackend.model.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDate;

@Entity
@Table(name = "history")
public class History {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "modified_field", nullable = false)
    private String modifiedField;
    @Column(name = "old_value")
    private String oldValue;
    @Column(name = "new_value")
    private String newValue;
    @Column(name = "modified_date")
    private LocalDate modifiedDate;
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "modified_by", nullable = false)
    private User modifiedBy;

    public History() {}
    public History(String modifiedField, String oldValue, String newValue, LocalDate modifiedDate, Task task, User modifiedBy) { this.modifiedField = modifiedField; this.oldValue = oldValue; this.newValue = newValue; this.modifiedDate = modifiedDate; this.task = task; this.modifiedBy = modifiedBy; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getModifiedField() { return modifiedField; }
    public void setModifiedField(String modifiedField) { this.modifiedField = modifiedField; }
    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }
    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
    public LocalDate getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(LocalDate modifiedDate) { this.modifiedDate = modifiedDate; }
    public Task getTask() { return task; }
    public void setTask(Task task) { this.task = task; }
    public User getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(User modifiedBy) { this.modifiedBy = modifiedBy; }
}
