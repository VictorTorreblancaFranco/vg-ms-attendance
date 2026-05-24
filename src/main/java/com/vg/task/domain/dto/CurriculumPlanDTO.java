package com.vg.task.domain.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CurriculumPlanDTO(
    Long id,
    @NotNull(message = "Class ID is required") Integer classId,
    @NotNull(message = "Unidad number is required") Integer unidadNumber,
    @NotNull(message = "Unidad name is required") String unidadName,
    @NotNull(message = "Tema name is required") String temaName,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    String objetivos,
    String competencias,
    Integer createdBy
) {}
