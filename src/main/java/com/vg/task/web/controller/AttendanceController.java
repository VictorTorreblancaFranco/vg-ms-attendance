package com.vg.task.web.controller;

import com.vg.task.domain.dto.AttendanceDTO;
import com.vg.task.domain.dto.PageResponseDTO;
import com.vg.task.service.impl.AttendanceServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceServiceImpl attendanceService;

    @GetMapping("/class/{classId}")
    public Flux<AttendanceDTO> findByClassId(@PathVariable Integer classId) {
        return attendanceService.findByClassId(classId);
    }

    @GetMapping("/class/{classId}/paged")
    public Mono<PageResponseDTO<AttendanceDTO>> findByClassIdPaged(
            @PathVariable Integer classId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return attendanceService.findByClassIdPaged(classId, page, size);
    }

    @GetMapping("/class/{classId}/date/{date}")
    public Flux<AttendanceDTO> findByClassIdAndDate(@PathVariable Integer classId, @PathVariable String date) {
        return attendanceService.findByClassIdAndDate(classId, LocalDate.parse(date));
    }

    @GetMapping("/student/{studentId}")
    public Flux<AttendanceDTO> findByStudentId(@PathVariable Integer studentId) {
        return attendanceService.findByStudentId(studentId);
    }

    @GetMapping("/student/{studentId}/paged")
    public Mono<PageResponseDTO<AttendanceDTO>> findByStudentIdPaged(
            @PathVariable Integer studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return attendanceService.findByStudentIdPaged(studentId, page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AttendanceDTO> save(@Valid @RequestBody AttendanceDTO dto) {
        return attendanceService.save(dto);
    }

    @PutMapping("/{id}")
    public Mono<AttendanceDTO> update(@PathVariable Long id, @Valid @RequestBody AttendanceDTO dto) {
        return attendanceService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id) {
        return attendanceService.delete(id);
    }

    @GetMapping("/template")
    public Mono<byte[]> getTemplate() {
        return attendanceService.getTemplate();
    }

    @GetMapping("/export/{classId}/{date}")
    public Mono<byte[]> exportToExcel(@PathVariable Integer classId, @PathVariable String date) {
        return attendanceService.exportToExcel(classId, LocalDate.parse(date));
    }
}
