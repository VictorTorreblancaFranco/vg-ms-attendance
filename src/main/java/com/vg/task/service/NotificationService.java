package com.vg.task.service;

import com.vg.task.domain.model.Notification;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NotificationService {
    Mono<Notification> send(Integer userId, Long taskId, String type, String title, String message);
    Mono<Void> markAsRead(Long id);
    Flux<Notification> getByUser(Integer userId);
    Flux<Notification> getUnreadByUser(Integer userId);
}
