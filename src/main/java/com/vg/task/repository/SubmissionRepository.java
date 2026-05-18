package com.vg.task.repository;

import com.vg.task.domain.model.Submission;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SubmissionRepository extends ReactiveCrudRepository<Submission, Long> {
    Flux<Submission> findByTaskId(Long taskId);
    Flux<Submission> findByStudentId(Integer studentId);
    Mono<Submission> findByTaskIdAndStudentId(Long taskId, Integer studentId);
    Mono<Boolean> existsByTaskIdAndStudentId(Long taskId, Integer studentId);
}
