package com.vg.task.domain.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record StudentDashboardDTO(
    Integer studentId,
    String studentName,
    List<MateriaDTO> materias
) {
    public record MateriaDTO(
        Integer materiaId,
        String materiaNombre,
        Integer gradoId,
        String gradoNombre,
        List<TareaDTO> tareas
    ) {}
    
    public record TareaDTO(
        Long taskId,
        String titulo,
        String descripcion,
        OffsetDateTime fechaEntrega,
        Boolean entregada,
        Double nota,
        String estado
    ) {}
}
