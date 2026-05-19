package com.vg.task.service;

import com.vg.task.domain.dto.RubricCriteriaRequestDTO;
import com.vg.task.domain.dto.RubricCriteriaResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RubricCriteriaService {
    Mono<RubricCriteriaResponseDTO> create(RubricCriteriaRequestDTO request);
    Flux<RubricCriteriaResponseDTO> findByTaskId(Long taskId);
}
