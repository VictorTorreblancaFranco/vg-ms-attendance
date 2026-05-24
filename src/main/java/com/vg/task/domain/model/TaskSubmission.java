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
@Table("task_submissions")
public class TaskSubmission {

    @Id
    private Long id;

    @Column("task_id")
    private Long taskId;

    @Column("student_id")
    private Integer studentId;

    @Column("attempt_number")
    private Short attemptNumber;

    @Column("submission_date")
    private OffsetDateTime submissionDate;

    private String status;
    private Double grade;
    private String feedback;

    @Column("graded_by")
    private Integer gradedBy;

    @Column("graded_at")
    private OffsetDateTime gradedAt;

    @Column("student_comment")
    private String studentComment;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
