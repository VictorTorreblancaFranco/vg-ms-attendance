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
@Table("rubric_scores")
public class RubricScore {
    @Id
    private Long id;
    @Column("submission_id")
    private Long submissionId;
    @Column("criterion_id")
    private Long criterionId;
    private Double score;
    private String feedback;
    @Column("created_at")
    private OffsetDateTime createdAt;
    @Column("updated_at")
    private OffsetDateTime updatedAt;
    @Column("active")
    private Boolean active;
}
