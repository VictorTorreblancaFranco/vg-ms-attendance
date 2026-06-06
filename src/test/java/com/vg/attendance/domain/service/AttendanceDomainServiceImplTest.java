package com.vg.attendance.domain.service;

import com.vg.attendance.domain.exception.BusinessException;
import com.vg.attendance.domain.model.Attendance;
import com.vg.attendance.domain.service.impl.AttendanceDomainServiceImpl;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class AttendanceDomainServiceImplTest {

    private final AttendanceDomainServiceImpl service = new AttendanceDomainServiceImpl();

    @Test
    void validateAttendanceRejectsLateStatusWithoutArrivalTime() {
        Attendance attendance = Attendance.builder()
            .estado("T")
            .build();

        StepVerifier.create(service.validateAttendance(attendance))
            .expectErrorMatches(error -> error instanceof BusinessException
                && error.getMessage().equals("La tardanza requiere hora de llegada"))
            .verify();
    }

    @Test
    void validateAttendanceRejectsJustifiedStatusWithoutNote() {
        Attendance attendance = Attendance.builder()
            .estado("J")
            .justificacionNota(" ")
            .build();

        StepVerifier.create(service.validateAttendance(attendance))
            .expectErrorMatches(error -> error instanceof BusinessException
                && error.getMessage().equals("La justificación requiere una nota"))
            .verify();
    }

    @Test
    void calculateLateMinutesReturnsOnlyPositiveDelay() {
        assertThat(service.calculateLateMinutes(LocalTime.of(8, 15), LocalTime.of(8, 0))).isEqualTo(15);
        assertThat(service.calculateLateMinutes(LocalTime.of(7, 55), LocalTime.of(8, 0))).isZero();
        assertThat(service.calculateLateMinutes(null, LocalTime.of(8, 0))).isZero();
    }
}
