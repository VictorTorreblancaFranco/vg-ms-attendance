package com.vg.task.application.port.input;

import com.vg.task.domain.model.Task;
import com.vg.task.domain.dto.TaskFilterDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TaskUseCase {
    Flux<Task> findAll();
    Mono<Task> findById(Long id);
    Flux<Task> findByStatus(String status);
    Flux<Task> findByClassId(Integer classId);
    Flux<Task> filter(TaskFilterDTO filter);
    Mono<Task> save(Task task);
    Mono<Task> update(Task task);
    Mono<Void> deleteById(Long id);
    Mono<Task> activate(Long id);
    Mono<Task> deactivate(Long id);
    Mono<Task> close(Long id);
    Mono<Task> restore(Long id);
    Mono<byte[]> exportToCsv(TaskFilterDTO filter);
    Mono<byte[]> exportToExcel(TaskFilterDTO filter);
}
