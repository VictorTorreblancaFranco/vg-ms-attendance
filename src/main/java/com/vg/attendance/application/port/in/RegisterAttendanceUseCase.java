package com.vg.attendance.application.port.in;

import com.vg.attendance.application.port.in.command.RegisterAttendanceCommand;
import com.vg.attendance.application.port.in.dto.AttendanceResponse;
import reactor.core.publisher.Mono;

public interface RegisterAttendanceUseCase {
    Mono<AttendanceResponse> registerAttendance(RegisterAttendanceCommand command);
}
