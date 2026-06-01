package com.vg.attendance.infrastructure.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceUpdateRequest {
    private String estado;
    private LocalTime horaLlegada;
    private String justificacionNota;
    private String justificacionFotoUrl;
    private String motivoCambio;
}
