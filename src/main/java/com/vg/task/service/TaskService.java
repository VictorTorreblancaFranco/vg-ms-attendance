package com.vg.task.service;

import com.vg.task.domain.dto.TaskRequestDTO;
import com.vg.task.domain.dto.TaskResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TaskService {
    Flux<TaskResponseDTO> findAll();
    Mono<TaskResponseDTO> findById(Long id);
    Mono<TaskResponseDTO> create(TaskRequestDTO request);
    Mono<TaskResponseDTO> update(Long id, TaskRequestDTO request);
    Mono<Void> delete(Long id);
    Flux<TaskResponseDTO> findByClassId(Integer classId);
    Flux<TaskResponseDTO> findByStatus(String status);
    Mono<TaskResponseDTO> publish(Long id);
    Mono<TaskResponseDTO> close(Long id);
}
