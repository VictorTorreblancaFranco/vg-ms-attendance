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

    @Column("private_comment")
    private String privateComment;

    @Column("public_comment")
    private String publicComment;

    @Column("reattempt_count")
    private Integer reattemptCount;

    @Column("reattempt_allowed")
    private Boolean reattemptAllowed;

    @Column("max_reattempts")
    private Integer maxReattempts;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
