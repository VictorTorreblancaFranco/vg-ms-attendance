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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {
    
    private final RegisterAttendanceUseCase registerUseCase;
    private final GetAttendanceUseCase getUseCase;
    private final UpdateAttendanceUseCase updateUseCase;
    private final AttendanceWebMapper mapper;
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AttendanceResponse> registerAttendance(@Valid @RequestBody AttendanceRequest request) {
        RegisterAttendanceCommand command = mapper.toCommand(request);
        return registerUseCase.registerAttendance(command);
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
