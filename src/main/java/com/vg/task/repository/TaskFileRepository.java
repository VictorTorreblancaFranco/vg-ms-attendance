package com.vg.task.repository;

import com.vg.task.domain.model.TaskFile;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface TaskFileRepository extends ReactiveCrudRepository<TaskFile, Long> {
    Flux<TaskFile> findByTaskId(Long taskId);
}
