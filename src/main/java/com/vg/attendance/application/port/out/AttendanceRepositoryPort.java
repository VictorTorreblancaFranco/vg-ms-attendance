package com.vg.attendance.application.port.out;

import com.vg.attendance.domain.model.Attendance;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface AttendanceRepositoryPort {
    Mono<Attendance> save(Attendance attendance);
    Mono<Attendance> findById(Long id);
    Flux<Attendance> findByEstudianteId(String estudianteId);
    Flux<Attendance> findByClaseIdAndFecha(String claseId, LocalDate fecha);
    Flux<Attendance> findByEstudianteIdAndFechaBetween(String estudianteId, LocalDate startDate, LocalDate endDate);
    Mono<Boolean> existsByEstudianteIdAndClaseIdAndFecha(String estudianteId, String claseId, LocalDate fecha);
    Mono<Void> deleteById(Long id);
}
