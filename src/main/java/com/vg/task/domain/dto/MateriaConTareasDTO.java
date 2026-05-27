package com.vg.task.domain.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record MateriaConTareasDTO(
    Integer materiaId,
    String materiaNombre,
    Integer gradoId,
    String gradoNombre,
    List<TareaPendienteDTO> tareasPendientes
) {
    public record TareaPendienteDTO(
        Long taskId,
        String titulo,
        String descripcion,
        OffsetDateTime fechaEntrega,
        Boolean yaEntregue,
        Double miNota,
        String estado
    ) {}
}
