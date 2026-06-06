package com.vg.attendance.infrastructure.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Pattern;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceUpdateRequest {
    @Pattern(regexp = "A|F|T|J", message = "El estado debe ser A, F, T o J")
    private String estado;
    private LocalTime horaLlegada;
    private String justificacionNota;
    private String justificacionFotoUrl;
    private String motivoCambio;
}
