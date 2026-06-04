package com.vg.attendance.infrastructure.adapter.in.web.controller;

import com.vg.attendance.application.port.in.GetAttendanceUseCase;
import com.vg.attendance.application.port.in.RegisterAttendanceUseCase;
import com.vg.attendance.application.port.in.UpdateAttendanceUseCase;
import com.vg.attendance.application.port.in.command.RegisterAttendanceCommand;
import com.vg.attendance.application.port.in.command.UpdateAttendanceCommand;
import com.vg.attendance.application.port.in.dto.AttendanceResponse;
import com.vg.attendance.infrastructure.adapter.in.web.dto.AttendanceRequest;
import com.vg.attendance.infrastructure.adapter.in.web.dto.AttendanceUpdateRequest;
import com.vg.attendance.infrastructure.adapter.in.web.mapper.AttendanceWebMapper;
import com.vg.attendance.infrastructure.adapter.out.client.EnrollmentClient;
import com.vg.attendance.infrastructure.adapter.out.client.ScheduleClient;
import com.vg.attendance.infrastructure.adapter.out.client.dto.EnrollmentResponse;
import com.vg.attendance.infrastructure.adapter.out.client.dto.ScheduleResponse;
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
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AttendanceResponse> registerAttendance(
            @Valid @RequestBody AttendanceRequest request,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestHeader("X-User-Id") String teacherIdFromGateway) {
        
        log.info("Registrando asistencia - Teacher from gateway: {}", teacherIdFromGateway);
        
        // Validar que el profesor que intenta registrar es el mismo del token
        if (!teacherIdFromGateway.equals(request.getProfesorId())) {
            return Mono.error(new RuntimeException("No puedes registrar asistencia para otro profesor"));
        }
        
        RegisterAttendanceCommand command = mapper.toCommand(request);
        return registerUseCase.registerAttendance(command);
    }
    
    @GetMapping("/teacher/{teacherId}/today")
    public Flux<ScheduleResponse> getTeacherTodayClasses(@PathVariable String teacherId) {
        log.info("Obteniendo clases de hoy para profesor: {}", teacherId);
        return scheduleClient.getTodayClassesByTeacher(teacherId);
    }
    
    @GetMapping("/teacher/{teacherId}/schedule")
    public Flux<ScheduleResponse> getTeacherSchedule(@PathVariable String teacherId) {
        log.info("Obteniendo horario completo del profesor: {}", teacherId);
        return scheduleClient.getClassesByTeacher(teacherId);
    }
    
    @GetMapping("/class/{gradeId}/{sectionId}/students")
    public Flux<EnrollmentResponse> getStudentsByClass(
            @PathVariable Long gradeId,
            @PathVariable Long sectionId,
            @RequestParam Long yearId) {
        log.info("Obteniendo alumnos para grado: {}, sección: {}, año: {}", gradeId, sectionId, yearId);
        return enrollmentClient.getStudentsByGradeSectionYear(gradeId, sectionId, yearId);
    }
    
    @GetMapping("/{id}")
    public Mono<AttendanceResponse> getAttendanceById(@PathVariable Long id) {
        return getUseCase.getAttendanceById(id);
    }
    
    @GetMapping("/student/{estudianteId}")
    public Flux<AttendanceResponse> getAttendanceByStudent(@PathVariable String estudianteId) {
        return getUseCase.getAttendanceByStudent(estudianteId);
    }
    
    @GetMapping("/class/{claseId}")
    public Flux<AttendanceResponse> getAttendanceByClass(
            @PathVariable String claseId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return getUseCase.getAttendanceByClass(claseId, fecha);
    }
    
    @GetMapping("/student/{estudianteId}/range")
    public Flux<AttendanceResponse> getAttendanceByDateRange(
            @PathVariable String estudianteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return getUseCase.getAttendanceByDateRange(estudianteId, startDate, endDate);
    }
    
    @PutMapping("/{id}")
    public Mono<AttendanceResponse> updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceUpdateRequest request) {
        UpdateAttendanceCommand command = mapper.toCommand(request);
        return updateUseCase.updateAttendance(id, command);
    }
    
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteAttendance(@PathVariable Long id) {
        return updateUseCase.deleteAttendance(id);
    }
}
