package com.vg.task.service;

import com.vg.task.domain.dto.AttendanceBulkDTO;
import com.vg.task.domain.dto.AttendanceDTO;
import com.vg.task.domain.dto.PageResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    Flux<AttendanceDTO> findByClassId(Integer classId);
    Mono<PageResponseDTO<AttendanceDTO>> findByClassIdPaged(Integer classId, int page, int size);
    Flux<AttendanceDTO> findByClassIdAndDate(Integer classId, LocalDate date);
    Flux<AttendanceDTO> findByStudentId(Integer studentId);
    Mono<PageResponseDTO<AttendanceDTO>> findByStudentIdPaged(Integer studentId, int page, int size);
    Mono<AttendanceDTO> save(AttendanceDTO dto);
    Mono<AttendanceDTO> update(Long id, AttendanceDTO dto);
    Mono<Void> delete(Long id);
    Mono<byte[]> getTemplate();
    Mono<byte[]> exportToExcel(Integer classId, LocalDate date);
    
    // NUEVO: Carga masiva de asistencias
    Mono<List<AttendanceDTO>> saveBulk(AttendanceBulkDTO bulkDTO);
}
