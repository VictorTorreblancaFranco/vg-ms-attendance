package com.vg.task.service.impl;

import com.vg.task.application.port.input.SubmissionUseCase;
import com.vg.task.application.port.output.SubmissionRepositoryPort;
import com.vg.task.application.port.output.StudentServicePort;
import com.vg.task.domain.model.Submission;
import com.vg.task.domain.dto.GradeRequestDTO;
import com.vg.task.domain.dto.RubricGradeRequestDTO;
import com.vg.task.domain.dto.PageResponseDTO;
import com.vg.task.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionUseCase {
    
    private final SubmissionRepositoryPort submissionRepository;
    private final StudentServicePort studentService;
    
    @Override
    public Flux<Submission> findAll() {
        return submissionRepository.findAll();
    }
    
    @Override
    public Mono<Submission> findById(Long id) {
        return submissionRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Submission not found: " + id)));
    }
    
    @Override
    public Flux<Submission> findByTaskId(Long taskId) {
        return submissionRepository.findByTaskId(taskId);
    }
    
    @Override
    public Mono<PageResponseDTO<Submission>> findByTaskIdPaged(Long taskId, int page, int size) {
        int offset = page * size;
        return submissionRepository.countByTaskId(taskId)
            .flatMap(total -> {
                if (total == 0) {
                    return Mono.just(PageResponseDTO.of(new ArrayList<>(), page, size, total));
                }
                return submissionRepository.findByTaskId(taskId, page, size)
                    .collectList()
                    .map(submissions -> PageResponseDTO.of(submissions, page, size, total));
            });
    }
    
    @Override
    public Flux<Submission> findByStudentId(Integer studentId) {
        return submissionRepository.findByStudentId(studentId);
    }
    
    @Override
    public Mono<PageResponseDTO<Submission>> findByStudentIdPaged(Integer studentId, int page, int size) {
        int offset = page * size;
        return submissionRepository.countByStudentId(studentId)
            .flatMap(total -> {
                if (total == 0) {
                    return Mono.just(PageResponseDTO.of(new ArrayList<>(), page, size, total));
                }
                return submissionRepository.findByStudentId(studentId, page, size)
                    .collectList()
                    .map(submissions -> PageResponseDTO.of(submissions, page, size, total));
            });
    }
    
    @Override
    public Mono<Submission> findByTaskIdAndStudentId(Long taskId, Integer studentId) {
        return submissionRepository.findByTaskIdAndStudentId(taskId, studentId);
    }
    
    @Override
    public Mono<Submission> submit(Submission submission) {
        return studentService.validateStudent(submission.getStudentId())
                .flatMap(valid -> {
                    if (!valid) {
                        return Mono.error(new NotFoundException("Student not found: " + submission.getStudentId()));
                    }
                    submission.setSubmissionDate(OffsetDateTime.now());
                    submission.setStatus("submitted");
                    submission.setCreatedAt(OffsetDateTime.now());
                    submission.setUpdatedAt(OffsetDateTime.now());
                    return submissionRepository.save(submission);
                });
    }
    
    @Override
    public Mono<Submission> grade(Long id, GradeRequestDTO request) {
        return submissionRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Submission not found: " + id)))
                .flatMap(submission -> {
                    submission.setGrade(request.grade());
                    submission.setFeedback(request.feedback());
                    submission.setGradedBy(request.gradedBy());
                    submission.setGradedAt(OffsetDateTime.now());
                    submission.setStatus("graded");
                    submission.setUpdatedAt(OffsetDateTime.now());
                    return submissionRepository.save(submission);
                });
    }
    
    @Override
    public Mono<Submission> gradeWithRubric(Long id, RubricGradeRequestDTO request) {
        return submissionRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Submission not found: " + id)))
                .flatMap(submission -> {
                    Double totalScore = request.scores().stream()
                            .mapToDouble(item -> item.score())
                            .sum();
                    submission.setGrade(totalScore);
                    submission.setGradedBy(request.gradedBy());
                    submission.setGradedAt(OffsetDateTime.now());
                    submission.setStatus("graded");
                    submission.setUpdatedAt(OffsetDateTime.now());
                    return submissionRepository.save(submission);
                });
    }
    
    @Override
    public Mono<Submission> allowReattempt(Long id, Integer maxAttempts) {
        return submissionRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Submission not found: " + id)))
                .flatMap(submission -> {
                    submission.setReattemptAllowed(true);
                    submission.setMaxReattempts(maxAttempts != null ? maxAttempts : 1);
                    submission.setUpdatedAt(OffsetDateTime.now());
                    return submissionRepository.save(submission);
                });
    }
    
    @Override
    public Mono<Submission> excuse(Long id, String reason) {
        return submissionRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Submission not found: " + id)))
                .flatMap(submission -> {
                    submission.setStatus("excused");
                    submission.setJustificationReason(reason);
                    submission.setUpdatedAt(OffsetDateTime.now());
                    return submissionRepository.save(submission);
                });
    }
    
    @Override
    public Mono<Void> delete(Long id) {
        return submissionRepository.deleteById(id);
    }
}
