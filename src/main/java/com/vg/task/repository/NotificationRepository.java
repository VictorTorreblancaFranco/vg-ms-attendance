package com.vg.task.repository;

import com.vg.task.domain.model.Notification;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface NotificationRepository extends ReactiveCrudRepository<Notification, Long> {
    Flux<Notification> findByUserId(Integer userId);
    Flux<Notification> findByUserIdAndIsReadFalse(Integer userId);
}
