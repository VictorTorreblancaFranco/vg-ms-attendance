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
@Table("rubric_criteria")
public class RubricCriteria {
    @Id
    private Long id;
    @Column("task_id")
    private Long taskId;
    private String name;
    private String description;
    @Column("max_score")
    private Double maxScore;
    private Double weight;
    @Column("sort_order")
    private Integer sortOrder;
    @Column("created_at")
    private OffsetDateTime createdAt;
}
