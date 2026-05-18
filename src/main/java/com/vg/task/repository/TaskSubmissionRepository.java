package com.vg.task.repository;

import com.vg.task.domain.model.TaskSubmission;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TaskSubmissionRepository extends ReactiveCrudRepository<TaskSubmission, Long> {
    Flux<TaskSubmission> findByTaskId(Long taskId);
    Flux<TaskSubmission> findByStudentId(Integer studentId);
    Mono<TaskSubmission> findByTaskIdAndStudentIdAndAttemptNumber(Long taskId, Integer studentId, Short attemptNumber);
    Mono<Integer> countByTaskIdAndStudentId(Long taskId, Integer studentId);
}
