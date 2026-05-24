package com.vg.task.web.controller;

import com.vg.task.domain.model.Notification;
import com.vg.task.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public Flux<Notification> getByUser(@PathVariable Integer userId) {
        return notificationService.getByUser(userId);
    }

    @GetMapping("/user/{userId}/unread")
    public Flux<Notification> getUnreadByUser(@PathVariable Integer userId) {
        return notificationService.getUnreadByUser(userId);
    }

    @PatchMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> markAsRead(@PathVariable Long id) {
        return notificationService.markAsRead(id);
    }
}
