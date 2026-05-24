package com.vg.task.service;

import com.vg.task.domain.dto.AttendanceDTO;
import com.vg.task.domain.dto.AttendanceBulkDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    Flux<AttendanceDTO> findByClassId(Integer classId);
    Flux<AttendanceDTO> findByClassIdAndDate(Integer classId, LocalDate date);
    Flux<AttendanceDTO> findByStudentId(Integer studentId);
    Mono<AttendanceDTO> save(AttendanceDTO dto);
    Mono<AttendanceDTO> update(Long id, AttendanceDTO dto);
    Mono<Void> delete(Long id);
    Mono<List<AttendanceDTO>> bulkSave(List<AttendanceBulkDTO> list, Integer classId, LocalDate date);
    Mono<byte[]> exportToExcel(Integer classId, LocalDate date);
    Mono<byte[]> getTemplate();
}
