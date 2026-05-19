package com.vg.task.web.handler;

import com.vg.task.domain.dto.DashboardStatsDTO;
import com.vg.task.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class DashboardHandler {

    private final DashboardService dashboardService;

    public Mono<ServerResponse> getDashboardStats(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(dashboardService.getDashboardStats(), DashboardStatsDTO.class);
    }

    public Mono<ServerResponse> getTaskStats(ServerRequest request) {
        Long taskId = Long.parseLong(request.pathVariable("taskId"));
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(dashboardService.getTaskStats(taskId), DashboardStatsDTO.class);
    }
}
