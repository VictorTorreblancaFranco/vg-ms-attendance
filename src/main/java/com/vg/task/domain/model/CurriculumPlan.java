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
@Table("curriculum_plan")
public class CurriculumPlan {
    @Id
    private Long id;
    @Column("class_id")
    private Integer classId;
    @Column("unidad_number")
    private Integer unidadNumber;
    @Column("unidad_name")
    private String unidadName;
    @Column("tema_name")
    private String temaName;
    @Column("fecha_inicio")
    private LocalDate fechaInicio;
    @Column("fecha_fin")
    private LocalDate fechaFin;
    private String objetivos;
    private String competencias;
    @Column("created_by")
    private Integer createdBy;
    @Column("created_at")
    private OffsetDateTime createdAt;
    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
