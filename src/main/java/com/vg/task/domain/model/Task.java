package com.vg.task.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tasks")
public class Task {
    
    @Id
    private Long id;
    private String title;
    private String description;
    private String instructions;
    
    @Column("class_id")
    private Integer classId;
    
    @Column("criterion_id")
    private Integer criterionId;
    
    @Column("points_value")
    private Double pointsValue;
    
    @Column("due_date")
    private OffsetDateTime dueDate;
    
    @Column("scheduled_publish_date")
    private OffsetDateTime scheduledPublishDate;
    
    @Column("scheduled_close_date")
    private OffsetDateTime scheduledCloseDate;
    
    private String status;
    
    @Column("is_deleted")
    private Boolean isDeleted;
    
    @Column("created_by")
    private Integer createdBy;
    
    @Column("created_at")
    private OffsetDateTime createdAt;
    
    @Column("updated_at")
    private OffsetDateTime updatedAt;
    
    @Column("deleted_at")
    private OffsetDateTime deletedAt;
}
