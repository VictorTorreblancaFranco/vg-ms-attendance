package com.vg.attendance.application.service;

import com.vg.attendance.application.port.in.GetAttendanceUseCase;
import com.vg.attendance.application.port.in.RegisterAttendanceUseCase;
import com.vg.attendance.application.port.in.UpdateAttendanceUseCase;
import com.vg.attendance.application.port.in.command.RegisterAttendanceCommand;
import com.vg.attendance.application.port.in.command.UpdateAttendanceCommand;
import com.vg.attendance.application.port.in.dto.AttendanceResponse;
import com.vg.attendance.application.port.out.AttendanceRepositoryPort;
import com.vg.attendance.domain.model.Attendance;
import com.vg.attendance.domain.valueobject.AttendanceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceApplicationService implements 
        RegisterAttendanceUseCase, 
        GetAttendanceUseCase, 
        UpdateAttendanceUseCase {
    
    private final AttendanceRepositoryPort attendanceRepository;
    
    @Override
    public Mono<AttendanceResponse> registerAttendance(RegisterAttendanceCommand command) {
        log.info("Registering attendance for student: {} in class: {}", command.getEstudianteId(), command.getClaseId());
        
        // Validar que no exista duplicado
        return attendanceRepository.existsByEstudianteIdAndClaseIdAndFecha(
                command.getEstudianteId(), command.getClaseId(), command.getFecha())
            .flatMap(exists -> {
                if (exists) {
                    return Mono.error(new RuntimeException("Ya existe un registro de asistencia para este estudiante en esta clase en esta fecha"));
                }
                
                // Validar estado
                AttendanceStatus status = AttendanceStatus.fromCode(command.getEstado());
                if (status == AttendanceStatus.TARDANZA && command.getHoraLlegada() == null) {
                    return Mono.error(new RuntimeException("La tardanza requiere hora de llegada"));
                }
                if (status == AttendanceStatus.JUSTIFICADO && command.getJustificacionNota() == null) {
                    return Mono.error(new RuntimeException("La justificación requiere una nota"));
                }
                
                // Crear entidad
                Attendance attendance = Attendance.builder()
                    .estudianteId(command.getEstudianteId())
                    .claseId(command.getClaseId())
                    .profesorId(command.getProfesorId())
                    .registradoPor(command.getRegistradoPor())
                    .fecha(command.getFecha())
                    .anioLectivo(command.getAnioLectivo())
                    .estado(command.getEstado())
                    .horaLlegada(command.getHoraLlegada())
                    .justificacionNota(command.getJustificacionNota())
                    .justificacionFotoUrl(command.getJustificacionFotoUrl())
                    .registradoEn(LocalDateTime.now())
                    .creadoEn(LocalDateTime.now())
                    .actualizadoEn(LocalDateTime.now())
                    .version(0)
                    .build();
                
                return attendanceRepository.save(attendance);
            })
            .map(this::toResponse);
    }
    
    @Override
    public Mono<AttendanceResponse> getAttendanceById(Long id) {
        return attendanceRepository.findById(id)
            .switchIfEmpty(Mono.error(new RuntimeException("Asistencia no encontrada con id: " + id)))
            .map(this::toResponse);
    }
    
    @Override
    public Flux<AttendanceResponse> getAttendanceByStudent(String estudianteId) {
        return attendanceRepository.findByEstudianteId(estudianteId)
            .map(this::toResponse);
    }
    
    @Override
    public Flux<AttendanceResponse> getAttendanceByClass(String claseId, LocalDate fecha) {
        return attendanceRepository.findByClaseIdAndFecha(claseId, fecha)
            .map(this::toResponse);
    }
    
    @Override
    public Flux<AttendanceResponse> getAttendanceByDateRange(String estudianteId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByEstudianteIdAndFechaBetween(estudianteId, startDate, endDate)
            .map(this::toResponse);
    }
    
    @Override
    public Mono<AttendanceResponse> updateAttendance(Long id, UpdateAttendanceCommand command) {
        return attendanceRepository.findById(id)
            .switchIfEmpty(Mono.error(new RuntimeException("Asistencia no encontrada con id: " + id)))
            .flatMap(attendance -> {
                if (command.getEstado() != null) {
                    attendance.setEstado(command.getEstado());
                }
                if (command.getHoraLlegada() != null) {
                    attendance.setHoraLlegada(command.getHoraLlegada());
                }
                if (command.getJustificacionNota() != null) {
                    attendance.setJustificacionNota(command.getJustificacionNota());
                }
                if (command.getJustificacionFotoUrl() != null) {
                    attendance.setJustificacionFotoUrl(command.getJustificacionFotoUrl());
                }
                attendance.setActualizadoEn(LocalDateTime.now());
                attendance.setVersion(attendance.getVersion() + 1);
                return attendanceRepository.save(attendance);
            })
            .map(this::toResponse);
    }
    
    @Override
    public Mono<Void> deleteAttendance(Long id) {
        return attendanceRepository.findById(id)
            .switchIfEmpty(Mono.error(new RuntimeException("Asistencia no encontrada con id: " + id)))
            .flatMap(attendance -> attendanceRepository.deleteById(id));
    }
    
    private AttendanceResponse toResponse(Attendance attendance) {
        AttendanceStatus status = AttendanceStatus.fromCode(attendance.getEstado());
        return AttendanceResponse.builder()
            .id(attendance.getId())
            .estudianteId(attendance.getEstudianteId())
            .claseId(attendance.getClaseId())
            .profesorId(attendance.getProfesorId())
            .registradoPor(attendance.getRegistradoPor())
            .fecha(attendance.getFecha())
            .anioLectivo(attendance.getAnioLectivo())
            .estado(attendance.getEstado())
            .estadoNombre(status.getDescription())
            .horaLlegada(attendance.getHoraLlegada())
            .justificacionNota(attendance.getJustificacionNota())
            .justificacionFotoUrl(attendance.getJustificacionFotoUrl())
            .registradoEn(attendance.getRegistradoEn())
            .creadoEn(attendance.getCreadoEn())
            .actualizadoEn(attendance.getActualizadoEn())
            .build();
    }
}
