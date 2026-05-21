package com.vg.task.application.port.output;

import com.vg.task.domain.model.Task;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TaskRepositoryPort {
    Flux<Task> findAll();
    Mono<Task> findById(Long id);
    Mono<Task> save(Task task);
    Mono<Void> deleteById(Long id);
    Flux<Task> findByStatus(String status);
    Flux<Task> findByClassId(Integer classId);
    Flux<Task> findByCreatedBy(Integer createdBy);
    Mono<Boolean> existsById(Long id);
}
