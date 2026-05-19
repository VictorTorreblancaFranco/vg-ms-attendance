package com.vg.task.service.impl;

import com.vg.task.domain.dto.RubricCriteriaRequestDTO;
import com.vg.task.domain.dto.RubricCriteriaResponseDTO;
import com.vg.task.domain.model.RubricCriteria;
import com.vg.task.repository.RubricCriteriaRepository;
import com.vg.task.repository.TaskRepository;
import com.vg.task.service.RubricCriteriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class RubricCriteriaServiceImpl implements RubricCriteriaService {

    private final RubricCriteriaRepository rubricCriteriaRepository;
    private final TaskRepository taskRepository;

    @Override
    public Mono<RubricCriteriaResponseDTO> create(RubricCriteriaRequestDTO request) {
        return taskRepository.findById(request.taskId())
                .switchIfEmpty(Mono.error(new RuntimeException("Task not found")))
                .flatMap(task -> {
                    RubricCriteria criteria = RubricCriteria.builder()
                            .taskId(request.taskId())
                            .name(request.name())
                            .description(request.description())
                            .maxScore(request.maxScore())
                            .weight(request.weight())
                            .sortOrder(request.sortOrder())
                            .createdAt(OffsetDateTime.now())
                            .build();
                    return rubricCriteriaRepository.save(criteria);
                })
                .map(this::toResponse);
    }

    @Override
    public Flux<RubricCriteriaResponseDTO> findByTaskId(Long taskId) {
        return rubricCriteriaRepository.findByTaskIdAndActiveTrueOrderBySortOrderAsc(taskId)
                .map(this::toResponse);
    }

    private RubricCriteriaResponseDTO toResponse(RubricCriteria criteria) {
        return new RubricCriteriaResponseDTO(
                criteria.getId(),
                criteria.getTaskId(),
                criteria.getName(),
                criteria.getDescription(),
                criteria.getMaxScore(),
                criteria.getWeight(),
                criteria.getSortOrder()
        );
    }
}
