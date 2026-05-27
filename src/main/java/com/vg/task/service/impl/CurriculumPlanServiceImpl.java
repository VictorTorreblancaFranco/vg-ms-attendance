package com.vg.task.service.impl;

import com.vg.task.domain.dto.CurriculumPlanDTO;
import com.vg.task.domain.model.CurriculumPlan;
import com.vg.task.domain.model.exceptions.NotFoundException;
import com.vg.task.repository.CurriculumPlanRepository;
import com.vg.task.service.CurriculumPlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurriculumPlanServiceImpl implements CurriculumPlanService {

    private final CurriculumPlanRepository curriculumPlanRepository;

    @Override
    public Flux<CurriculumPlanDTO> findByClassId(Integer classId) {
        return curriculumPlanRepository.findByClassId(classId)
                .map(this::toDTO);
    }

    @Override
    public Mono<CurriculumPlanDTO> findById(Long id) {
        return curriculumPlanRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Curriculum plan not found: " + id)))
                .map(this::toDTO);
    }

    @Override
    public Mono<CurriculumPlanDTO> save(CurriculumPlanDTO dto) {
        CurriculumPlan plan = CurriculumPlan.builder()
                .classId(dto.classId())
                .unidadNumber(dto.unidadNumber())
                .unidadName(dto.unidadName())
                .temaName(dto.temaName())
                .fechaInicio(dto.fechaInicio())
                .fechaFin(dto.fechaFin())
                .objetivos(dto.objetivos())
                .competencias(dto.competencias())
                .createdBy(dto.createdBy())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        return curriculumPlanRepository.save(plan)
                .map(this::toDTO);
    }

    @Override
    public Mono<CurriculumPlanDTO> update(Long id, CurriculumPlanDTO dto) {
        return curriculumPlanRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Curriculum plan not found: " + id)))
                .flatMap(existing -> {
                    if (dto.unidadNumber() != null) existing.setUnidadNumber(dto.unidadNumber());
                    if (dto.unidadName() != null) existing.setUnidadName(dto.unidadName());
                    if (dto.temaName() != null) existing.setTemaName(dto.temaName());
                    if (dto.fechaInicio() != null) existing.setFechaInicio(dto.fechaInicio());
                    if (dto.fechaFin() != null) existing.setFechaFin(dto.fechaFin());
                    if (dto.objetivos() != null) existing.setObjetivos(dto.objetivos());
                    if (dto.competencias() != null) existing.setCompetencias(dto.competencias());
                    existing.setUpdatedAt(OffsetDateTime.now());
                    return curriculumPlanRepository.save(existing);
                })
                .map(this::toDTO);
    }

    @Override
    public Mono<Void> delete(Long id) {
        return curriculumPlanRepository.deleteById(id);
    }

    private CurriculumPlanDTO toDTO(CurriculumPlan plan) {
        return new CurriculumPlanDTO(
                plan.getId(),
                plan.getClassId(),
                plan.getUnidadNumber(),
                plan.getUnidadName(),
                plan.getTemaName(),
                plan.getFechaInicio(),
                plan.getFechaFin(),
                plan.getObjetivos(),
                plan.getCompetencias(),
                plan.getCreatedBy()
        );
    }
}
