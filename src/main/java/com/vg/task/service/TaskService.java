package com.vg.task.service;

import com.vg.task.domain.dto.TaskRequestDTO;
import com.vg.task.domain.dto.TaskResponseDTO;
import com.vg.task.domain.dto.UpdateTaskRequestDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TaskService {
    Flux<TaskResponseDTO> findAll();
    Flux<TaskResponseDTO> findByStatus(String status);
    Flux<TaskResponseDTO> findByClassId(Integer classId);
    Mono<TaskResponseDTO> findById(Long id);
    Mono<TaskResponseDTO> save(TaskRequestDTO request);
    Mono<TaskResponseDTO> update(UpdateTaskRequestDTO request);
    Mono<Void> deleteById(Long id);
    Mono<TaskResponseDTO> activate(Long id);
    Mono<TaskResponseDTO> deactivate(Long id);
    Mono<TaskResponseDTO> close(Long id);
    Mono<TaskResponseDTO> restore(Long id);
}
