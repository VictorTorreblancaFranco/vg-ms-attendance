package com.vg.task.application.port.output;

import com.vg.task.domain.model.Submission;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SubmissionRepositoryPort {
    Flux<Submission> findAll();
    Mono<Submission> findById(Long id);
    Flux<Submission> findByTaskId(Long taskId);
    Flux<Submission> findByTaskId(Long taskId, int page, int size);
    Flux<Submission> findByStudentId(Integer studentId);
    Flux<Submission> findByStudentId(Integer studentId, int page, int size);
    Mono<Submission> findByTaskIdAndStudentId(Long taskId, Integer studentId);
    Mono<Submission> save(Submission submission);
    Mono<Void> deleteById(Long id);
    Mono<Boolean> existsByTaskIdAndStudentId(Long taskId, Integer studentId);
    Mono<Long> countByTaskId(Long taskId);
    Mono<Long> countByStudentId(Integer studentId);
}
