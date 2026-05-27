package com.vg.task.web.controller;

import com.vg.task.domain.dto.RubricCriteriaRequestDTO;
import com.vg.task.domain.dto.RubricCriteriaResponseDTO;
import com.vg.task.service.RubricCriteriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/task/rubric/criteria")
@RequiredArgsConstructor
public class RubricCriteriaController {
    private final RubricCriteriaService rubricCriteriaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<RubricCriteriaResponseDTO> create(@RequestBody RubricCriteriaRequestDTO request) {
        return rubricCriteriaService.create(request);
    }

    @GetMapping("/{taskId}")
    public Flux<RubricCriteriaResponseDTO> findByTaskId(@PathVariable Long taskId) {
        return rubricCriteriaService.findByTaskId(taskId);
    }
}
