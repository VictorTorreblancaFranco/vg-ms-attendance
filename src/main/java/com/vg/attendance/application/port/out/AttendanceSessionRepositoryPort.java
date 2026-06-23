package com.vg.attendance.application.port.out;

import com.vg.attendance.domain.model.AttendanceSession;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface AttendanceSessionRepositoryPort {
    Mono<AttendanceSession> save(AttendanceSession session);
    Mono<AttendanceSession> findByClaseIdAndFecha(String claseId, LocalDate fecha);
}
