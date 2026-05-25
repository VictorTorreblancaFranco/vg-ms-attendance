package com.vg.task.web.controller;

import com.vg.task.domain.dto.*;
import com.vg.task.service.impl.SubmissionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionServiceImpl submissionService;

    @GetMapping
    public Flux<SubmissionResponseDTO> findAll() {
        return submissionService.findAll();
    }

    @GetMapping("/paged")
    public Mono<PageResponseDTO<SubmissionResponseDTO>> findAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return submissionService.findAllPaged(page, size);
    }

    @GetMapping("/{id}")
    public Mono<SubmissionResponseDTO> findById(@PathVariable Long id) {
        return submissionService.findById(id);
    }

    @GetMapping("/task/{taskId}")
    public Flux<SubmissionResponseDTO> findByTaskId(@PathVariable Long taskId) {
        return submissionService.findByTaskId(taskId);
    }

    @GetMapping("/task/{taskId}/paged")
    public Mono<PageResponseDTO<SubmissionResponseDTO>> findByTaskIdPaged(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return submissionService.findByTaskIdPaged(taskId, page, size);
    }

    @GetMapping("/student/{studentId}")
    public Flux<SubmissionResponseDTO> findByStudentId(@PathVariable Integer studentId) {
        return submissionService.findByStudentId(studentId);
    }

    @GetMapping("/student/{studentId}/paged")
    public Mono<PageResponseDTO<SubmissionResponseDTO>> findByStudentIdPaged(
            @PathVariable Integer studentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return submissionService.findByStudentIdPaged(studentId, page, size);
    }

    @PostMapping("/submit")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SubmissionResponseDTO> submit(@RequestBody SubmissionRequestDTO request) {
        return submissionService.submit(request);
    }

    @PutMapping("/{id}/grade")
    public Mono<SubmissionResponseDTO> grade(@PathVariable Long id, @RequestBody GradeRequestDTO request) {
        return submissionService.grade(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id) {
        return submissionService.delete(id);
    }
}
