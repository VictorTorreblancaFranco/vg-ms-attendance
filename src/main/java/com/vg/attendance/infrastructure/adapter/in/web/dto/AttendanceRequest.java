package com.vg.attendance.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class AttendanceRequest {
    
    @NotBlank
    private String estudianteId;
    
    @NotBlank
    private String claseId;
    
    @NotBlank
    private String profesorId;
    
    @NotBlank
    private String registradoPor;
    
    @NotNull
    private LocalDate fecha;
    
    @NotNull
    private Integer anioLectivo;
    
    @NotBlank
    private String estado;
    
    private LocalTime horaLlegada;
    
    private String justificacionNota;
    
    private String justificacionFotoUrl;
}
