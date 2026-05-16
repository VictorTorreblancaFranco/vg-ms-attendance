package com.vg.task.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassDTO {
    private Integer id;
    private Integer profesorId;
    private Integer gradoId;
    private Integer materiaId;
    private Integer anoAcademicoId;
    private Boolean activa;
}
