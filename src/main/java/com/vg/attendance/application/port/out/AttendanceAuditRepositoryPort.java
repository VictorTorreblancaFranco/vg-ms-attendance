package com.vg.attendance.application.port.out;

import com.vg.attendance.domain.model.AttendanceAudit;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AttendanceAuditRepositoryPort {
    Mono<AttendanceAudit> save(AttendanceAudit audit);
    Flux<AttendanceAudit> findByAttendanceId(Long attendanceId);
}
