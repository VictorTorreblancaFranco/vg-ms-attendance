package com.vg.attendance.domain.service;

import com.vg.attendance.domain.model.Attendance;
import reactor.core.publisher.Mono;
import java.time.LocalTime;

public interface AttendanceDomainService {
    Mono<Attendance> validateAttendance(Attendance attendance);
    int calculateLateMinutes(LocalTime arrivalTime, LocalTime classStartTime);
    String getStatusDescription(String statusCode);
}
