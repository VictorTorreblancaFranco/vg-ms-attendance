package com.vg.task.application.port.input;

import com.vg.task.domain.model.Submission;
import com.vg.task.domain.dto.GradeRequestDTO;
import com.vg.task.domain.dto.RubricGradeRequestDTO;
import com.vg.task.domain.dto.PageResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SubmissionUseCase {
    Flux<Submission> findAll();
    Mono<Submission> findById(Long id);
    Flux<Submission> findByTaskId(Long taskId);
    Mono<PageResponseDTO<Submission>> findByTaskIdPaged(Long taskId, int page, int size);
    Flux<Submission> findByStudentId(Integer studentId);
    Mono<PageResponseDTO<Submission>> findByStudentIdPaged(Integer studentId, int page, int size);
    Mono<Submission> findByTaskIdAndStudentId(Long taskId, Integer studentId);
    Mono<Submission> submit(Submission submission);
    Mono<Submission> grade(Long id, GradeRequestDTO request);
    Mono<Submission> gradeWithRubric(Long id, RubricGradeRequestDTO request);
    Mono<Submission> allowReattempt(Long id, Integer maxAttempts);
    Mono<Submission> excuse(Long id, String reason);
    Mono<Void> delete(Long id);
}
