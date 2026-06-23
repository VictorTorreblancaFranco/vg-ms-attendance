package com.vg.attendance.application.port.in;

import com.vg.attendance.application.port.in.command.UpdateAttendanceCommand;
import com.vg.attendance.application.port.in.dto.AttendanceResponse;
import reactor.core.publisher.Mono;

public interface UpdateAttendanceUseCase {
    Mono<AttendanceResponse> updateAttendance(Long id, UpdateAttendanceCommand command);
}
