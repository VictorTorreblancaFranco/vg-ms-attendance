package com.vg.attendance.application.port.in.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {
    private Long id;
    private String estudianteId;
    private String claseId;
    private String profesorId;
    private String registradoPor;
    private LocalDate fecha;
    private Integer anioLectivo;
    private String estado;
    private String estadoNombre;
    private LocalTime horaLlegada;
    private String justificacionNota;
    private String justificacionFotoUrl;
    private LocalDateTime registradoEn;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
