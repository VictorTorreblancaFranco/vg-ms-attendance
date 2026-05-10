package com.vg.task.model.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tareas")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "class_id", nullable = false)
    private Integer classId;
    
    @Column(name = "criterion_id")
    private Integer criterionId;
    
    private String title;
    private String description;
    private String instructions;
    
    @Column(name = "assignment_date")
    private LocalDate assignmentDate;
    
    @Column(name = "due_date")
    private LocalDate dueDate;
    
    @Column(name = "points_value")
    private Double pointsValue;
    
    @Column(name = "allowed_attempts")
    private Short allowedAttempts;
    
    @Column(name = "is_group_task")
    private Boolean isGroupTask;
    
    @Column(name = "visible_to_parents")
    private Boolean visibleToParents;
    
    private String status;
    
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    // Getters
    public Long getId() { return id; }
    public Integer getClassId() { return classId; }
    public Integer getCriterionId() { return criterionId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getInstructions() { return instructions; }
    public LocalDate getAssignmentDate() { return assignmentDate; }
    public LocalDate getDueDate() { return dueDate; }
    public Double getPointsValue() { return pointsValue; }
    public Short getAllowedAttempts() { return allowedAttempts; }
    public Boolean getIsGroupTask() { return isGroupTask; }
    public Boolean getVisibleToParents() { return visibleToParents; }
    public String getStatus() { return status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setClassId(Integer classId) { this.classId = classId; }
    public void setCriterionId(Integer criterionId) { this.criterionId = criterionId; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public void setAssignmentDate(LocalDate assignmentDate) { this.assignmentDate = assignmentDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setPointsValue(Double pointsValue) { this.pointsValue = pointsValue; }
    public void setAllowedAttempts(Short allowedAttempts) { this.allowedAttempts = allowedAttempts; }
    public void setIsGroupTask(Boolean isGroupTask) { this.isGroupTask = isGroupTask; }
    public void setVisibleToParents(Boolean visibleToParents) { this.visibleToParents = visibleToParents; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
