package com.vg.attendance.infrastructure.adapter.out.persistence;

import com.vg.attendance.domain.model.AttendanceSession;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
public interface AttendanceSessionR2dbcRepository extends R2dbcRepository<AttendanceSession, Long> {
    Mono<AttendanceSession> findByClaseIdAndFecha(String claseId, LocalDate fecha);
}
