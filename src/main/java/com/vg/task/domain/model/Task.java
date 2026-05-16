package com.vg.task.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tasks")
public class Task {

    @Id
    private Long id;

    @Column("class_id")
    private Integer classId;

    @Column("criterion_id")
    private Integer criterionId;

    private String title;
    private String description;
    private String instructions;

    @Column("assignment_date")
    private LocalDate assignmentDate;

    @Column("due_date")
    private LocalDate dueDate;

    @Column("points_value")
    private Double pointsValue;

    @Column("allowed_attempts")
    private Short allowedAttempts;

    @Column("is_group_task")
    private Boolean isGroupTask;

    @Column("visible_to_parents")
    private Boolean visibleToParents;

    private String status;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
