package com.vg.task.service;

import com.vg.task.domain.dto.GradeRequestDTO;
import com.vg.task.domain.dto.SubmissionRequestDTO;
import com.vg.task.domain.dto.SubmissionResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SubmissionService {
    Flux<SubmissionResponseDTO> findAll();
    Flux<SubmissionResponseDTO> findByTaskId(Long taskId);
    Flux<SubmissionResponseDTO> findByStudentId(Integer studentId);
    Mono<SubmissionResponseDTO> findById(Long id);
    Mono<SubmissionResponseDTO> submit(SubmissionRequestDTO request);
    Mono<SubmissionResponseDTO> grade(Long id, GradeRequestDTO request);
    Mono<SubmissionResponseDTO> excuse(Long id, String reason);
    Mono<Void> delete(Long id);
}
