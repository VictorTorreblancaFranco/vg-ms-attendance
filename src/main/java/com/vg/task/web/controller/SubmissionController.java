package com.vg.task.web.controller;

import com.vg.task.domain.dto.SubmissionRequestDTO;
import com.vg.task.domain.dto.SubmissionResponseDTO;
import com.vg.task.domain.dto.GradeRequestDTO;
import com.vg.task.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/submissions")
@RequiredArgsConstructor
public class SubmissionController {
    private final SubmissionService submissionService;

    @GetMapping
    public Flux<SubmissionResponseDTO> findAll() { return submissionService.findAll(); }
    @GetMapping("/{id}")
    public Mono<SubmissionResponseDTO> findById(@PathVariable Long id) { return submissionService.findById(id); }
    @GetMapping("/task/{taskId}")
    public Flux<SubmissionResponseDTO> findByTaskId(@PathVariable Long taskId) { return submissionService.findByTaskId(taskId); }
    @GetMapping("/student/{studentId}")
    public Flux<SubmissionResponseDTO> findByStudentId(@PathVariable Integer studentId) { return submissionService.findByStudentId(studentId); }
    @PostMapping("/submit")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SubmissionResponseDTO> submit(@RequestBody SubmissionRequestDTO request) { return submissionService.submit(request); }
    @PutMapping("/{id}/grade")
    public Mono<SubmissionResponseDTO> grade(@PathVariable Long id, @RequestBody GradeRequestDTO request) { return submissionService.grade(id, request); }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id) { return submissionService.delete(id); }
}
