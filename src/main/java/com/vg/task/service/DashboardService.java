package com.vg.task.service;

import com.vg.task.domain.dto.DashboardStatsDTO;
import reactor.core.publisher.Mono;

public interface DashboardService {
    Mono<DashboardStatsDTO> getDashboardStats();
    Mono<DashboardStatsDTO> getTaskStats(Long taskId);
}
