package com.vg.attendance.application.port.in;

import com.vg.attendance.application.port.in.dto.AttendanceAuditResponse;
import reactor.core.publisher.Flux;

public interface GetAttendanceAuditUseCase {
    Flux<AttendanceAuditResponse> getAuditByAttendanceId(Long attendanceId);
}
