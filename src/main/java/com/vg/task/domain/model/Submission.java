package com.vg.task.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.OffsetDateTime;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("submissions")
public class Submission {
    @Id
    private Long id;
    @Column("task_id")
    private Long taskId;
    @Column("student_id")
    private Integer studentId;
    @Column("submission_date")
    private OffsetDateTime submissionDate;
    private String status;
    private Double grade;
    private String feedback;
    @Column("graded_by")
    private Integer gradedBy;
    @Column("graded_at")
    private OffsetDateTime gradedAt;
    @Column("justification_reason")
    private String justificationReason;
    private Boolean presented;
    @Column("presented_at")
    private OffsetDateTime presentedAt;
    private String observations;
    @Column("is_late")
    private Boolean isLate;
    @Column("justified_at")
    private OffsetDateTime justifiedAt;
    @Column("justified_by")
    private Integer justifiedBy;
    @Column("created_at")
    private OffsetDateTime createdAt;
    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
