package com.vg.attendance.application.port.out;

import com.vg.attendance.domain.model.Attendance;
import reactor.core.publisher.Mono;

public interface NotificationPort {
    Mono<Void> notifyAbsenceOrLate(Attendance attendance);

    Mono<Void> notifyThreeFullAbsenceDays(String estudianteId, int absenceDays);
}
