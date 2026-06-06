package com.vg.attendance.application.service;

import com.vg.attendance.application.port.in.command.RegisterAttendanceCommand;
import com.vg.attendance.application.port.in.command.UpdateAttendanceCommand;
import com.vg.attendance.application.port.out.AttendanceRepositoryPort;
import com.vg.attendance.domain.exception.BusinessException;
import com.vg.attendance.domain.exception.ConflictException;
import com.vg.attendance.domain.exception.NotFoundException;
import com.vg.attendance.domain.model.Attendance;
import com.vg.attendance.domain.service.impl.AttendanceDomainServiceImpl;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AttendanceApplicationServiceTest {

    private final InMemoryAttendanceRepository repository = new InMemoryAttendanceRepository();
    private final AttendanceApplicationService service =
        new AttendanceApplicationService(repository, new AttendanceDomainServiceImpl());

    @Test
    void registerAttendanceCreatesRecordWhenItDoesNotExist() {
        RegisterAttendanceCommand command = RegisterAttendanceCommand.builder()
            .estudianteId("student-1")
            .claseId("class-1")
            .profesorId("teacher-1")
            .registradoPor("teacher-1")
            .fecha(LocalDate.of(2026, 6, 5))
            .anioLectivo(2026)
            .estado("A")
            .build();

        StepVerifier.create(service.registerAttendance(command))
            .assertNext(response -> {
                assertThat(response.getId()).isEqualTo(1L);
                assertThat(response.getEstado()).isEqualTo("A");
                assertThat(response.getEstadoNombre()).isEqualTo("Asistió");
                assertThat(response.getCreadoEn()).isNotNull();
            })
            .verifyComplete();
    }

    @Test
    void registerAttendanceRejectsDuplicateStudentClassDate() {
        repository.save(existingAttendance(1L)).block();

        RegisterAttendanceCommand command = RegisterAttendanceCommand.builder()
            .estudianteId("student-1")
            .claseId("class-1")
            .profesorId("teacher-1")
            .registradoPor("teacher-1")
            .fecha(LocalDate.of(2026, 6, 5))
            .anioLectivo(2026)
            .estado("A")
            .build();

        StepVerifier.create(service.registerAttendance(command))
            .expectError(ConflictException.class)
            .verify();
    }

    @Test
    void updateAttendanceAppliesChangesAndIncrementsVersion() {
        repository.save(existingAttendance(7L)).block();

        UpdateAttendanceCommand command = UpdateAttendanceCommand.builder()
            .estado("T")
            .horaLlegada(LocalTime.of(8, 12))
            .build();

        StepVerifier.create(service.updateAttendance(7L, command))
            .assertNext(response -> {
                assertThat(response.getEstado()).isEqualTo("T");
                assertThat(response.getHoraLlegada()).isEqualTo(LocalTime.of(8, 12));
            })
            .verifyComplete();

        StepVerifier.create(repository.findById(7L))
            .assertNext(saved -> assertThat(saved.getVersion()).isEqualTo(1))
            .verifyComplete();
    }

    @Test
    void deleteAttendanceReturnsNotFoundWhenRecordDoesNotExist() {
        StepVerifier.create(service.deleteAttendance(404L))
            .expectError(NotFoundException.class)
            .verify();
    }

    @Test
    void getAttendanceByDateRangeRejectsInvalidRange() {
        StepVerifier.create(service.getAttendanceByDateRange(
                "student-1",
                LocalDate.of(2026, 6, 10),
                LocalDate.of(2026, 6, 5)))
            .expectError(BusinessException.class)
            .verify();
    }

    private Attendance existingAttendance(Long id) {
        return Attendance.builder()
            .id(id)
            .estudianteId("student-1")
            .claseId("class-1")
            .profesorId("teacher-1")
            .registradoPor("teacher-1")
            .fecha(LocalDate.of(2026, 6, 5))
            .anioLectivo(2026)
            .estado("A")
            .registradoEn(LocalDateTime.now())
            .creadoEn(LocalDateTime.now())
            .actualizadoEn(LocalDateTime.now())
            .version(0)
            .build();
    }

    private static class InMemoryAttendanceRepository implements AttendanceRepositoryPort {
        private final Map<Long, Attendance> records = new HashMap<>();
        private long sequence = 0;

        @Override
        public Mono<Attendance> save(Attendance attendance) {
            if (attendance.getId() == null) {
                attendance.setId(++sequence);
            }
            records.put(attendance.getId(), attendance);
            return Mono.just(attendance);
        }

        @Override
        public Mono<Attendance> findById(Long id) {
            return Mono.justOrEmpty(records.get(id));
        }

        @Override
        public Flux<Attendance> findByEstudianteId(String estudianteId) {
            return Flux.fromIterable(records.values())
                .filter(record -> estudianteId.equals(record.getEstudianteId()));
        }

        @Override
        public Flux<Attendance> findByClaseIdAndFecha(String claseId, LocalDate fecha) {
            return Flux.fromIterable(records.values())
                .filter(record -> claseId.equals(record.getClaseId()) && fecha.equals(record.getFecha()));
        }

        @Override
        public Flux<Attendance> findByEstudianteIdAndFechaBetween(String estudianteId, LocalDate startDate, LocalDate endDate) {
            return Flux.fromIterable(records.values())
                .filter(record -> estudianteId.equals(record.getEstudianteId()))
                .filter(record -> !record.getFecha().isBefore(startDate) && !record.getFecha().isAfter(endDate));
        }

        @Override
        public Mono<Boolean> existsByEstudianteIdAndClaseIdAndFecha(String estudianteId, String claseId, LocalDate fecha) {
            return Flux.fromIterable(records.values())
                .any(record -> estudianteId.equals(record.getEstudianteId())
                    && claseId.equals(record.getClaseId())
                    && fecha.equals(record.getFecha()));
        }

        @Override
        public Mono<Void> deleteById(Long id) {
            records.remove(id);
            return Mono.empty();
        }

        @Override
        public Flux<Attendance> findByFecha(LocalDate fecha) {
            return Flux.fromIterable(records.values())
                .filter(record -> fecha.equals(record.getFecha()));
        }

        @Override
        public Flux<Attendance> findRecentAttendanceByStudent(String estudianteId) {
            return findByEstudianteId(estudianteId).take(30);
        }
    }
}
