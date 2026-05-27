package com.vg.task.web.controller;

import com.vg.task.domain.dto.StudentDashboardDTO;
import com.vg.task.service.StudentDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class StudentDashboardController {

    private final StudentDashboardService studentDashboardService;

    @GetMapping("/student/{studentId}")
    public Mono<StudentDashboardDTO> getStudentDashboard(@PathVariable Integer studentId) {
        log.info("📱 GET /dashboard/student/{}", studentId);
        return studentDashboardService.getDashboard(studentId);
    }
}
