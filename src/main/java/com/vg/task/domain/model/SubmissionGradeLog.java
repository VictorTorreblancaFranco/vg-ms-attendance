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
@Table("submission_grade_logs")
public class SubmissionGradeLog {
    @Id
    private Long id;
    @Column("submission_id")
    private Long submissionId;
    @Column("old_grade")
    private Double oldGrade;
    @Column("new_grade")
    private Double newGrade;
    @Column("changed_by")
    private Integer changedBy;
    private String justification;
    @Column("changed_at")
    private OffsetDateTime changedAt;
}
