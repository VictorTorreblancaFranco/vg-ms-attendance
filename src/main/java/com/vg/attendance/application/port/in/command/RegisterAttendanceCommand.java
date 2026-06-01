package com.vg.attendance.application.port.in.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterAttendanceCommand {
    private String estudianteId;
    private String claseId;
    private String profesorId;
    private String registradoPor;
    private LocalDate fecha;
    private Integer anioLectivo;
    private String estado;
    private LocalTime horaLlegada;
    private String justificacionNota;
    private String justificacionFotoUrl;
}
