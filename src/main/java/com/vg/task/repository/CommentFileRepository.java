package com.vg.task.repository;

import com.vg.task.domain.model.CommentFile;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CommentFileRepository extends ReactiveCrudRepository<CommentFile, Long> {
    Flux<CommentFile> findBySubmissionId(Long submissionId);
}
