package com.vg.attendance.infrastructure.adapter.out.persistence;

import com.vg.attendance.application.port.out.AttendanceSessionRepositoryPort;
import com.vg.attendance.domain.model.AttendanceSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class AttendanceSessionRepositoryAdapter implements AttendanceSessionRepositoryPort {

    private final AttendanceSessionR2dbcRepository repository;

    @Override
    public Mono<AttendanceSession> save(AttendanceSession session) {
        return repository.save(session);
    }

    @Override
    public Mono<AttendanceSession> findByClaseIdAndFecha(String claseId, LocalDate fecha) {
        return repository.findByClaseIdAndFecha(claseId, fecha);
    }
}
