package com.vg.task.web.controller;

import com.vg.task.domain.dto.DashboardStatsDTO;
import com.vg.task.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping
    public Mono<DashboardStatsDTO> getDashboardStats() {
        return dashboardService.getDashboardStats();
    }

    @GetMapping("/task/{taskId}")
    public Mono<DashboardStatsDTO> getTaskStats(@PathVariable Long taskId) {
        return dashboardService.getTaskStats(taskId);
    }
}
