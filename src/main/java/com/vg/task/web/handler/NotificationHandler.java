package com.vg.task.web.handler;

import com.vg.task.domain.model.Notification;
import com.vg.task.service.impl.NotificationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class NotificationHandler {

    private final NotificationServiceImpl notificationService;

    public Mono<ServerResponse> getByUser(ServerRequest request) {
        Integer userId = Integer.parseInt(request.pathVariable("userId"));
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(notificationService.getByUser(userId), Notification.class);
    }

    public Mono<ServerResponse> getUnreadByUser(ServerRequest request) {
        Integer userId = Integer.parseInt(request.pathVariable("userId"));
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(notificationService.getUnreadByUser(userId), Notification.class);
    }

    public Mono<ServerResponse> markAsRead(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return notificationService.markAsRead(id)
                .then(ServerResponse.noContent().build());
    }
}
