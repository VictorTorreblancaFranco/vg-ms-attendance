package com.vg.task.repository;

import com.vg.task.domain.model.Task;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface TaskRepository extends ReactiveCrudRepository<Task, Long> {
    Flux<Task> findByClassId(Integer classId);
    Flux<Task> findByStatus(String status);
}
