package com.vg.attendance.infrastructure.adapter.in.web.controller;

import com.vg.attendance.application.port.in.GetAttendanceUseCase;
import com.vg.attendance.application.port.in.RegisterAttendanceUseCase;
import com.vg.attendance.application.port.in.UpdateAttendanceUseCase;
import com.vg.attendance.application.port.in.command.RegisterAttendanceCommand;
import com.vg.attendance.application.port.in.command.UpdateAttendanceCommand;
import com.vg.attendance.application.port.in.dto.AttendanceResponse;
import com.vg.attendance.domain.exception.BusinessException;
import com.vg.attendance.domain.exception.ForbiddenException;
import com.vg.attendance.infrastructure.adapter.in.web.dto.AttendanceRequest;
import com.vg.attendance.infrastructure.adapter.in.web.dto.AttendanceUpdateRequest;
import com.vg.attendance.infrastructure.adapter.in.web.mapper.AttendanceWebMapper;
import com.vg.attendance.infrastructure.adapter.out.client.EnrollmentClient;
import com.vg.attendance.infrastructure.adapter.out.client.ScheduleClient;
import com.vg.attendance.infrastructure.adapter.out.client.UserClient;
import com.vg.attendance.infrastructure.adapter.out.client.dto.EnrollmentResponse;
import com.vg.attendance.infrastructure.adapter.out.client.dto.ScheduleResponse;
import com.vg.attendance.infrastructure.adapter.out.client.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    
    private final RegisterAttendanceUseCase registerUseCase;
    private final GetAttendanceUseCase getUseCase;
    private final UpdateAttendanceUseCase updateUseCase;
    private final AttendanceWebMapper mapper;
    private final ScheduleClient scheduleClient;
    private final EnrollmentClient enrollmentClient;
    private final UserClient userClient;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AttendanceResponse> registerAttendance(
            @Valid @RequestBody AttendanceRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestHeader("X-User-Id") String teacherIdFromGateway,
            @RequestHeader(value = "X-User-Role", required = false) String roleFromGateway) {
        
        log.info("Registrando asistencia - User from gateway: {}, role: {}", teacherIdFromGateway, roleFromGateway);
        
        if (!canManageAnyTeacher(roleFromGateway) && !teacherIdFromGateway.equals(request.getProfesorId())) {
            return Mono.error(new ForbiddenException("No puedes registrar asistencia para otro profesor"));
        }

        request.setRegistradoPor(teacherIdFromGateway);
        
        RegisterAttendanceCommand command = mapper.toCommand(request);
        String token = extractToken(authHeader);
        return validateStudentEnrollmentForClass(request, authHeader, token)
                .then(registerUseCase.registerAttendance(command))
                .flatMap(response -> enrichAttendance(response, token));
    }
    
    @GetMapping("/teacher/{teacherId}/today")
    public Flux<ScheduleResponse> getTeacherTodayClasses(
            @PathVariable String teacherId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        log.info("Obteniendo clases de hoy para profesor: {}", teacherId);
        validateTeacherAccess(teacherId, userId, role);
        return scheduleClient.getTodayClassesByTeacher(teacherId, authHeader);
    }
    
    @GetMapping("/teacher/{teacherId}/schedule")
    public Flux<ScheduleResponse> getTeacherSchedule(
            @PathVariable String teacherId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        log.info("Obteniendo horario completo del profesor: {}", teacherId);
        validateTeacherAccess(teacherId, userId, role);
        return scheduleClient.getClassesByTeacher(teacherId, authHeader);
    }
    
    @GetMapping("/class/{gradeId}/{sectionId}/students")
    public Flux<EnrollmentResponse> getStudentsByClass(
            @PathVariable Long gradeId,
            @PathVariable Long sectionId,
            @RequestParam Long yearId,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        
        String token = authHeader.substring(7);
        log.info("Obteniendo alumnos para grado: {}, sección: {}, año: {}", gradeId, sectionId, yearId);
        return enrollmentClient.getStudentsByGradeSectionYear(gradeId, sectionId, yearId, token)
                .filter(student -> student.getIsActive() == null || Boolean.TRUE.equals(student.getIsActive()))
                .filter(student -> Objects.equals(student.getGradeId(), gradeId))
                .filter(student -> Objects.equals(student.getSectionId(), sectionId))
                .filter(student -> Objects.equals(student.getAcademicYearId(), yearId))
                .collectList()
                .flatMapMany(students -> enrichStudents(students, token));
    }
    
    @GetMapping("/{id}")
    public Mono<AttendanceResponse> getAttendanceById(
            @PathVariable Long id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        return getUseCase.getAttendanceById(id)
                .flatMap(response -> enrichAttendance(response, extractToken(authHeader)));
    }
    
    @GetMapping("/student/{estudianteId}")
    public Flux<AttendanceResponse> getAttendanceByStudent(
            @PathVariable String estudianteId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        String token = extractToken(authHeader);
        return getUseCase.getAttendanceByStudent(estudianteId)
                .collectList()
                .flatMapMany(attendances -> enrichAttendances(attendances, token));
    }
    
    @GetMapping("/class/{claseId}")
    public Flux<AttendanceResponse> getAttendanceByClass(
            @PathVariable String claseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        String token = extractToken(authHeader);
        return getUseCase.getAttendanceByClass(claseId, fecha)
                .collectList()
                .flatMapMany(attendances -> enrichAttendances(attendances, token));
    }
    
    @GetMapping("/student/{estudianteId}/range")
    public Flux<AttendanceResponse> getAttendanceByDateRange(
            @PathVariable String estudianteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        validateDateRange(startDate, endDate);
        String token = extractToken(authHeader);
        return getUseCase.getAttendanceByDateRange(estudianteId, startDate, endDate)
                .collectList()
                .flatMapMany(attendances -> enrichAttendances(attendances, token));
    }
    
    @PutMapping("/{id}")
    public Mono<AttendanceResponse> updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceUpdateRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        UpdateAttendanceCommand command = mapper.toCommand(request);
        String token = extractToken(authHeader);
        return getUseCase.getAttendanceById(id)
                .doOnNext(attendance -> validateAttendanceMutationAccess(attendance, userId, role))
                .then(updateUseCase.updateAttendance(id, command))
                .flatMap(response -> enrichAttendance(response, token));
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteAttendance(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        return getUseCase.getAttendanceById(id)
                .doOnNext(attendance -> validateAttendanceMutationAccess(attendance, userId, role))
                .then(updateUseCase.deleteAttendance(id));
    }

    private void validateTeacherAccess(String teacherId, String userId, String role) {
        if (!canManageAnyTeacher(role) && !teacherId.equals(userId)) {
            throw new ForbiddenException("No puedes consultar clases de otro profesor");
        }
    }

    private void validateAttendanceMutationAccess(AttendanceResponse attendance, String userId, String role) {
        if (canManageAnyTeacher(role)) {
            return;
        }

        if (!userId.equals(attendance.getProfesorId()) && !userId.equals(attendance.getRegistradoPor())) {
            throw new ForbiddenException("No puedes modificar asistencias de otro profesor");
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
    }

    private Mono<Void> validateStudentEnrollmentForClass(AttendanceRequest request, String authHeader, String token) {
        Long classId = parseClassId(request.getClaseId());

        return scheduleClient.getClassesByTeacher(request.getProfesorId(), authHeader)
                .filter(schedule -> Objects.equals(schedule.getId(), classId))
                .next()
                .switchIfEmpty(Mono.error(new BusinessException("La clase no existe o no pertenece al profesor indicado")))
                .flatMap(schedule -> enrollmentClient.getStudentsByGradeSectionYear(
                                schedule.getGradeId(),
                                schedule.getSectionId(),
                                schedule.getAcademicYearId(),
                                token)
                        .filter(student -> Objects.equals(student.getStudentId(), request.getEstudianteId()))
                        .filter(student -> student.getIsActive() == null || Boolean.TRUE.equals(student.getIsActive()))
                        .filter(student -> Objects.equals(student.getGradeId(), schedule.getGradeId()))
                        .filter(student -> Objects.equals(student.getSectionId(), schedule.getSectionId()))
                        .filter(student -> Objects.equals(student.getAcademicYearId(), schedule.getAcademicYearId()))
                        .next()
                        .switchIfEmpty(Mono.error(new BusinessException(
                                "El estudiante no está matriculado en el grado y sección de la clase"))))
                .then();
    }

    private Long parseClassId(String classId) {
        try {
            return Long.valueOf(classId);
        } catch (NumberFormatException error) {
            throw new BusinessException("El identificador de clase no es válido");
        }
    }

    private boolean canManageAnyTeacher(String role) {
        return Set.of("ADMIN", "DEVELOPER", "DIRECTOR", "COORDINATOR", "SECRETARY").contains(role);
    }

    private Flux<EnrollmentResponse> enrichStudents(List<EnrollmentResponse> students, String token) {
        if (students.isEmpty()) {
            return Flux.empty();
        }

        return loadUserNames(
                students.stream().map(EnrollmentResponse::getStudentId).filter(Objects::nonNull).distinct().toList(),
                token)
                .flatMapMany(names -> Flux.fromIterable(students)
                        .map(student -> {
                            student.setStudentName(names.getOrDefault(student.getStudentId(), shortId(student.getStudentId())));
                            return student;
                        }));
    }

    private Flux<AttendanceResponse> enrichAttendances(List<AttendanceResponse> attendances, String token) {
        if (attendances.isEmpty()) {
            return Flux.empty();
        }

        List<String> userIds = attendances.stream()
                .flatMap(attendance -> java.util.stream.Stream.of(
                        attendance.getEstudianteId(),
                        attendance.getProfesorId(),
                        attendance.getRegistradoPor()))
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return loadUserNames(userIds, token)
                .flatMapMany(names -> Flux.fromIterable(attendances)
                        .map(attendance -> applyNames(attendance, names)));
    }

    private Mono<AttendanceResponse> enrichAttendance(AttendanceResponse attendance, String token) {
        if (attendance == null) {
            return Mono.empty();
        }

        List<String> userIds = java.util.stream.Stream.of(
                        attendance.getEstudianteId(),
                        attendance.getProfesorId(),
                        attendance.getRegistradoPor())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return loadUserNames(userIds, token)
                .map(names -> applyNames(attendance, names));
    }

    private AttendanceResponse applyNames(AttendanceResponse attendance, Map<String, String> names) {
        attendance.setEstudianteNombre(names.getOrDefault(attendance.getEstudianteId(), shortId(attendance.getEstudianteId())));
        attendance.setProfesorNombre(names.getOrDefault(attendance.getProfesorId(), shortId(attendance.getProfesorId())));
        attendance.setRegistradoPorNombre(names.getOrDefault(attendance.getRegistradoPor(), shortId(attendance.getRegistradoPor())));
        return attendance;
    }

    private Mono<Map<String, String>> loadUserNames(List<String> userIds, String token) {
        if (userIds.isEmpty()) {
            return Mono.just(Map.of());
        }

        return Flux.fromIterable(userIds)
                .flatMap(userId -> resolveUserName(userId, token)
                        .map(name -> Map.entry(userId, name)))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .map(HashMap::new);
    }

    private Mono<String> resolveUserName(String userId, String token) {
        Mono<UserResponse> request = token == null || token.isBlank()
                ? userClient.getUserById(userId)
                : userClient.getUserById(userId, token);

        return request
                .map(this::formatUserName)
                .filter(name -> !name.isBlank())
                .defaultIfEmpty(shortId(userId));
    }

    private String formatUserName(UserResponse user) {
        if (user == null) {
            return "";
        }
        String fullName = ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                + (user.getLastName() == null ? "" : user.getLastName())).trim();
        return fullName.isBlank() ? (user.getEmail() == null ? "" : user.getEmail()) : fullName;
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }

    private String shortId(String id) {
        if (id == null || id.isBlank()) {
            return "Sin asignar";
        }
        return id.length() > 8 ? id.substring(id.length() - 8) : id;
    }

}
