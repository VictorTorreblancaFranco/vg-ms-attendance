package com.vg.attendance.infrastructure.adapter.out.persistence;

import com.vg.attendance.domain.model.Attendance;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
public interface AttendanceR2dbcRepository extends R2dbcRepository<Attendance, Long> {
    
    Flux<Attendance> findByEstudianteIdOrderByFechaDesc(String estudianteId);
    
    Flux<Attendance> findByClaseIdAndFecha(String claseId, LocalDate fecha);
    
    Flux<Attendance> findByEstudianteIdAndFechaBetween(String estudianteId, LocalDate startDate, LocalDate endDate);
    
    Flux<Attendance> findByFecha(LocalDate fecha);

    Flux<Attendance> findTop30ByEstudianteIdOrderByFechaDesc(String estudianteId);
    
    @Query("SELECT EXISTS(SELECT 1 FROM asistencias WHERE estudiante_id = $1 AND clase_id = $2 AND fecha = $3)")
    Mono<Boolean> existsByEstudianteIdAndClaseIdAndFecha(String estudianteId, String claseId, LocalDate fecha);

    @Query("SELECT COUNT(*) FROM asistencias WHERE session_id = $1")
    Mono<Long> countBySessionId(Long sessionId);
}
