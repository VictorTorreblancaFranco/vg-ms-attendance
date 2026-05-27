package com.vg.task.service;

import com.vg.task.domain.dto.CurriculumPlanDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CurriculumPlanService {
    Flux<CurriculumPlanDTO> findByClassId(Integer classId);
    Mono<CurriculumPlanDTO> findById(Long id);
    Mono<CurriculumPlanDTO> save(CurriculumPlanDTO dto);
    Mono<CurriculumPlanDTO> update(Long id, CurriculumPlanDTO dto);
    Mono<Void> delete(Long id);
}
