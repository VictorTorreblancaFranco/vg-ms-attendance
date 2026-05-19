package com.vg.task.repository;

import com.vg.task.domain.model.SubmissionFile;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface SubmissionFileRepository extends ReactiveCrudRepository<SubmissionFile, Long> {
    Flux<SubmissionFile> findBySubmissionId(Long submissionId);
    Flux<SubmissionFile> findBySubmissionIdAndActiveTrue(Long submissionId);
}
