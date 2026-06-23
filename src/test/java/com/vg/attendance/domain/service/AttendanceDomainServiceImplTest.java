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
    void validateAttendanceAllowsLateStatusWithOptionalJustificationNote() {
        Attendance attendance = Attendance.builder()
            .estado("T")
            .horaLlegada(LocalTime.of(8, 20))
            .justificacionNota("Llegó tarde con sustento")
            .build();

        StepVerifier.create(service.validateAttendance(attendance))
            .expectNext(attendance)
            .verifyComplete();
    }

    @Test
    void validateAttendanceRejectsAbsentStatusWithJustificationNote() {
        Attendance attendance = Attendance.builder()
            .estado("F")
            .justificacionNota("Debe ser J para justificar falta")
            .build();

        StepVerifier.create(service.validateAttendance(attendance))
            .expectErrorMatches(error -> error instanceof BusinessException
                && error.getMessage().equals("Solo una falta justificada o tardanza puede tener nota de justificación"))
            .verify();
    }

    @Test
    void calculateLateMinutesReturnsOnlyPositiveDelay() {
        assertThat(service.calculateLateMinutes(LocalTime.of(8, 15), LocalTime.of(8, 0))).isEqualTo(15);
        assertThat(service.calculateLateMinutes(LocalTime.of(7, 55), LocalTime.of(8, 0))).isZero();
        assertThat(service.calculateLateMinutes(null, LocalTime.of(8, 0))).isZero();
    }
}
