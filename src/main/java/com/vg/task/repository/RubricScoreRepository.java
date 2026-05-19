package com.vg.task.repository;

import com.vg.task.domain.model.RubricScore;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RubricScoreRepository extends ReactiveCrudRepository<RubricScore, Long> {
    Flux<RubricScore> findBySubmissionId(Long submissionId);
    Flux<RubricScore> findBySubmissionIdAndActiveTrue(Long submissionId);
    Mono<RubricScore> findBySubmissionIdAndCriterionId(Long submissionId, Long criterionId);
}
