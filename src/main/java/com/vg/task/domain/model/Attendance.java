package com.vg.task.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("attendance")
public class Attendance {
    @Id
    private Long id;
    @Column("class_id")
    private Integer classId;
    @Column("student_id")
    private Integer studentId;
    private LocalDate date;
    private String status;  // A, F, J, T
    private String observation;
    @Column("created_by")
    private Integer createdBy;
    @Column("created_at")
    private OffsetDateTime createdAt;
    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
