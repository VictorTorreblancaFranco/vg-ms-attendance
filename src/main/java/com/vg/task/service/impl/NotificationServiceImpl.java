package com.vg.task.service.impl;

import com.vg.task.domain.model.Notification;
import com.vg.task.domain.model.exceptions.NotFoundException;
import com.vg.task.repository.NotificationRepository;
import com.vg.task.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public Mono<Notification> send(Integer userId, Long taskId, String type, String title, String message) {
        Notification notification = Notification.builder()
                .userId(userId)
                .taskId(taskId)
                .type(type)
                .title(title)
                .message(message)
                .isRead(false)
                .createdAt(OffsetDateTime.now())
                .build();
        return notificationRepository.save(notification);
    }

    @Override
    public Mono<Void> markAsRead(Long id) {
        return notificationRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Notification not found")))
                .flatMap(n -> {
                    n.setIsRead(true);
                    return notificationRepository.save(n);
                })
                .then();
    }

    @Override
    public Flux<Notification> getByUser(Integer userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Override
    public Flux<Notification> getUnreadByUser(Integer userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }
}
