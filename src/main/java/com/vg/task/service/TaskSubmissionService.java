package com.vg.task.service;

import com.vg.task.domain.dto.GradeSubmissionRequestDTO;
import com.vg.task.domain.dto.TaskSubmissionRequestDTO;
import com.vg.task.domain.dto.TaskSubmissionResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TaskSubmissionService {
    Flux<TaskSubmissionResponseDTO> findAll();
    Mono<TaskSubmissionResponseDTO> findById(Long id);
    Mono<TaskSubmissionResponseDTO> submit(TaskSubmissionRequestDTO request, Integer userId);
    Mono<TaskSubmissionResponseDTO> grade(Long id, GradeSubmissionRequestDTO request);
    Flux<TaskSubmissionResponseDTO> findByTaskId(Long taskId);
    Flux<TaskSubmissionResponseDTO> findByStudentId(Integer studentId);
    Mono<Void> delete(Long id);
}
