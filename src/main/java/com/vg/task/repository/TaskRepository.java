package com.vg.task.repository;

import com.vg.task.domain.model.Task;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;

@Repository
public interface TaskRepository extends ReactiveCrudRepository<Task, Long> {
    Flux<Task> findByStatus(String status);
    Flux<Task> findByClassId(Integer classId);
    Flux<Task> findByStatusAndClassId(String status, Integer classId);
    Flux<Task> findByCreatedByAndIsDeletedFalse(Integer createdBy);
    Flux<Task> findByCreatedAtBetween(OffsetDateTime from, OffsetDateTime to);
    Flux<Task> findByDueDateBetween(OffsetDateTime from, OffsetDateTime to);
    Flux<Task> findByScheduledPublishDateBeforeAndStatusAndIsDeletedFalse(OffsetDateTime date, String status);
    Flux<Task> findByScheduledCloseDateBeforeAndStatusAndIsDeletedFalse(OffsetDateTime date, String status);
    Flux<Task> findByDueDateBeforeAndStatusAndIsDeletedFalse(OffsetDateTime date, String status);
    Flux<Task> findByDueDateBetweenAndStatusAndIsDeletedFalse(OffsetDateTime from, OffsetDateTime to, String status);
}
