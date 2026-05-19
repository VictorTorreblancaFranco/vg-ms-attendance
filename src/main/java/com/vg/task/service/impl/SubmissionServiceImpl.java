package com.vg.task.service.impl;

import com.vg.task.client.StudentClient;
import com.vg.task.domain.dto.*;
import com.vg.task.domain.model.*;
import com.vg.task.exception.BadRequestException;
import com.vg.task.exception.NotFoundException;
import com.vg.task.repository.*;
import com.vg.task.service.NotificationService;
import com.vg.task.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final SubmissionFileRepository submissionFileRepository;
    private final CommentFileRepository commentFileRepository;
    private final RubricCriteriaRepository rubricCriteriaRepository;
    private final RubricScoreRepository rubricScoreRepository;
    private final TaskRepository taskRepository;
    private final StudentClient studentClient;
    private final NotificationService notificationService;

    @Override
    public Flux<SubmissionResponseDTO> findAll() {
        return submissionRepository.findAll()
                .flatMap(this::toResponse);
    }

    @Override
    public Flux<SubmissionResponseDTO> findByTaskId(Long taskId) {
        return submissionRepository.findByTaskId(taskId)
                .flatMap(this::toResponse);
    }

    @Override
    public Flux<SubmissionResponseDTO> findByStudentId(Integer studentId) {
        return submissionRepository.findByStudentId(studentId)
                .flatMap(this::toResponse);
    }

    @Override
    public Mono<SubmissionResponseDTO> findById(Long id) {
        return submissionRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Submission not found")))
                .flatMap(this::toResponse);
    }

    @Override
    public Mono<SubmissionResponseDTO> submit(SubmissionRequestDTO request) {
        return taskRepository.findById(request.taskId())
                .switchIfEmpty(Mono.error(new NotFoundException("Task not found")))
                .flatMap(task -> {
                    if (!"published".equals(task.getStatus())) {
                        return Mono.error(new BadRequestException("Task is not published yet"));
                    }
                    return studentClient.validateStudent(request.studentId())
                        .flatMap(valid -> {
                            if (!valid) {
                                return Mono.error(new NotFoundException("Student not found"));
                            }
                            return submissionRepository.findByTaskIdAndStudentId(request.taskId(), request.studentId())
                                .flatMap(existing -> {
                                    if (existing != null && (existing.getReattemptAllowed() == null || !existing.getReattemptAllowed())) {
                                        return Mono.error(new BadRequestException("You have already submitted and reattempts are not allowed"));
                                    }
                                    return createSubmission(task, request, existing);
                                })
                                .switchIfEmpty(createSubmission(task, request, null));
                        });
                })
                .flatMap(this::toResponse);
    }

    private Mono<Submission> createSubmission(Task task, SubmissionRequestDTO request, Submission existing) {
        OffsetDateTime now = OffsetDateTime.now();
        String status = now.isAfter(task.getDueDate()) ? "late" : "submitted";
        
        int attemptCount = existing != null ? existing.getReattemptCount() + 1 : 1;
        
        Submission submission = Submission.builder()
                .taskId(request.taskId())
                .studentId(request.studentId())
                .submissionDate(now)
                .status(status)
                .justificationReason(request.justificationReason())
                .privateComment(request.privateComment())
                .publicComment(request.publicComment())
                .reattemptCount(attemptCount)
                .reattemptAllowed(false)
                .maxReattempts(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
        
        if (existing != null) {
            submission.setId(existing.getId());
        }
        
        return submissionRepository.save(submission)
                .flatMap(saved -> {
                    if (request.files() != null && !request.files().isEmpty()) {
                        List<SubmissionFile> files = new ArrayList<>();
                        for (SubmissionFileDTO f : request.files()) {
                            files.add(SubmissionFile.builder()
                                    .submissionId(saved.getId())
                                    .fileName(f.fileName())
                                    .fileUrl(f.fileUrl())
                                    .fileType(f.fileType())
                                    .fileSizeKb(f.fileSizeKb())
                                    .createdAt(now)
                                    .build());
                        }
                        return submissionFileRepository.saveAll(files)
                                .collectList()
                                .thenReturn(saved);
                    }
                    return Mono.just(saved);
                });
    }

    @Override
    public Mono<SubmissionResponseDTO> gradeWithRubric(Long id, RubricGradeRequestDTO request) {
        return submissionRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Submission not found")))
                .flatMap(sub -> {
                    Double totalScore = 0.0;
                    for (RubricScoreItemDTO item : request.scores()) {
                        totalScore += item.score();
                    }
                    Double finalGrade = totalScore;
                    
                    sub.setGrade(finalGrade);
                    sub.setPrivateComment(request.privateComment());
                    sub.setPublicComment(request.publicComment());
                    sub.setGradedBy(request.gradedBy());
                    sub.setGradedAt(OffsetDateTime.now());
                    sub.setStatus("graded");
                    sub.setUpdatedAt(OffsetDateTime.now());
                    
                    return submissionRepository.save(sub)
                        .flatMap(saved -> {
                            List<Mono<RubricScore>> scoreMonos = new ArrayList<>();
                            for (RubricScoreItemDTO item : request.scores()) {
                                RubricScore score = RubricScore.builder()
                                        .submissionId(saved.getId())
                                        .criterionId(item.criterionId())
                                        .score(item.score())
                                        .feedback(item.feedback())
                                        .createdAt(OffsetDateTime.now())
                                        .updatedAt(OffsetDateTime.now())
                                        .build();
                                scoreMonos.add(rubricScoreRepository.save(score));
                            }
                            return Flux.concat(scoreMonos).collectList().thenReturn(saved);
                        })
                        .flatMap(saved -> notificationService.send(
                                saved.getStudentId(),
                                saved.getTaskId(),
                                "SUBMISSION_GRADED",
                                "Tarea Calificada",
                                "Tu tarea ha sido calificada con: " + finalGrade
                        ).thenReturn(saved));
                })
                .flatMap(this::toResponse);
    }

    @Override
    public Mono<SubmissionResponseDTO> allowReattempt(Long id, Integer maxAttempts) {
        return submissionRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Submission not found")))
                .flatMap(sub -> {
                    sub.setReattemptAllowed(true);
                    sub.setMaxReattempts(maxAttempts != null ? maxAttempts : 1);
                    sub.setStatus("reattempt_allowed");
                    sub.setUpdatedAt(OffsetDateTime.now());
                    return submissionRepository.save(sub);
                })
                .flatMap(this::toResponse);
    }

    @Override
    public Mono<SubmissionResponseDTO> grade(Long id, GradeRequestDTO request) {
        return submissionRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Submission not found")))
                .flatMap(sub -> {
                    sub.setGrade(request.grade());
                    sub.setFeedback(request.feedback());
                    sub.setPrivateComment(request.privateComment());
                    sub.setPublicComment(request.publicComment());
                    sub.setGradedBy(request.gradedBy());
                    sub.setGradedAt(OffsetDateTime.now());
                    sub.setStatus("graded");
                    sub.setUpdatedAt(OffsetDateTime.now());
                    return submissionRepository.save(sub)
                            .flatMap(saved -> {
                                if (request.commentFiles() != null && !request.commentFiles().isEmpty()) {
                                    List<CommentFile> files = new ArrayList<>();
                                    for (CommentFileDTO f : request.commentFiles()) {
                                        files.add(CommentFile.builder()
                                                .submissionId(saved.getId())
                                                .fileName(f.fileName())
                                                .fileUrl(f.fileUrl())
                                                .fileType(f.fileType())
                                                .fileSizeKb(f.fileSizeKb())
                                                .createdAt(OffsetDateTime.now())
                                                .build());
                                    }
                                    return commentFileRepository.saveAll(files)
                                            .collectList()
                                            .thenReturn(saved);
                                }
                                return Mono.just(saved);
                            })
                            .flatMap(saved -> notificationService.send(
                                    saved.getStudentId(),
                                    saved.getTaskId(),
                                    "SUBMISSION_GRADED",
                                    "Tarea Calificada",
                                    "Tu tarea ha sido calificada con: " + saved.getGrade()
                            ).thenReturn(saved));
                })
                .flatMap(this::toResponse);
    }

    @Override
    public Mono<SubmissionResponseDTO> excuse(Long id, String reason) {
        return submissionRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Submission not found")))
                .flatMap(sub -> {
                    sub.setStatus("excused");
                    sub.setJustificationReason(reason);
                    sub.setUpdatedAt(OffsetDateTime.now());
                    return submissionRepository.save(sub);
                })
                .flatMap(this::toResponse);
    }

    @Override
    public Mono<Void> delete(Long id) {
        return submissionRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Submission not found")))
                .flatMap(submissionRepository::delete);
    }

    private Mono<SubmissionResponseDTO> toResponse(Submission sub) {
        return Mono.zip(
            submissionFileRepository.findBySubmissionIdAndActiveTrue(sub.getId()).collectList(),
            commentFileRepository.findBySubmissionIdAndActiveTrue(sub.getId()).collectList(),
            rubricScoreRepository.findBySubmissionIdAndActiveTrue(sub.getId()).collectList()
        ).map(tuple -> {
            List<SubmissionFileDTO> files = tuple.getT1().stream()
                    .map(f -> new SubmissionFileDTO(f.getId(), f.getFileName(), f.getFileUrl(), f.getFileType(), f.getFileSizeKb()))
                    .toList();
            List<CommentFileDTO> commentFiles = tuple.getT2().stream()
                    .map(f -> new CommentFileDTO(f.getId(), f.getFileName(), f.getFileUrl(), f.getFileType(), f.getFileSizeKb()))
                    .toList();
            List<RubricScoreDTO> rubricScores = tuple.getT3().stream()
                    .map(r -> new RubricScoreDTO(r.getId(), r.getCriterionId(), r.getScore(), r.getFeedback()))
                    .toList();
            
            return new SubmissionResponseDTO(
                    sub.getId(), sub.getTaskId(), sub.getStudentId(),
                    sub.getSubmissionDate(), sub.getStatus(), sub.getGrade(),
                    sub.getFeedback(), sub.getGradedBy(), sub.getGradedAt(),
                    sub.getJustificationReason(), sub.getPrivateComment(),
                    sub.getPublicComment(), sub.getReattemptCount(),
                    sub.getReattemptAllowed(), sub.getMaxReattempts(),
                    sub.getCreatedAt(), sub.getUpdatedAt(),
                    files, commentFiles, rubricScores
            );
        });
    }
}
