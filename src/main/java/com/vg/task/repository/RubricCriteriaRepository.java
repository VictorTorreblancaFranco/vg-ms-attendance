package com.vg.task.repository;

import com.vg.task.domain.model.RubricCriteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface RubricCriteriaRepository extends ReactiveCrudRepository<RubricCriteria, Long> {
    Flux<RubricCriteria> findByTaskIdOrderBySortOrderAsc(Long taskId);
}
