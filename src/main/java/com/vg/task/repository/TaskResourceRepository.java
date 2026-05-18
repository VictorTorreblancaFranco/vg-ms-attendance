package com.vg.task.repository;

import com.vg.task.domain.model.TaskResource;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface TaskResourceRepository extends ReactiveCrudRepository<TaskResource, Long> {
    Flux<TaskResource> findByTaskId(Long taskId);
}
