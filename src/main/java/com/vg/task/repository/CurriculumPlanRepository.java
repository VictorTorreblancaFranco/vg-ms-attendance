package com.vg.task.repository;

import com.vg.task.domain.model.CurriculumPlan;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CurriculumPlanRepository extends ReactiveCrudRepository<CurriculumPlan, Long> {
    Flux<CurriculumPlan> findByClassId(Integer classId);
}
