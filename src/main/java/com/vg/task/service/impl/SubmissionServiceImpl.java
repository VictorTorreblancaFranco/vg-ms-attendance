package com.vg.task.service.impl;

import com.vg.task.client.StudentClient;
import com.vg.task.domain.dto.GradeRequestDTO;
import com.vg.task.domain.dto.SubmissionFileDTO;
import com.vg.task.domain.dto.SubmissionRequestDTO;
import com.vg.task.domain.dto.SubmissionResponseDTO;
import com.vg.task.domain.model.Submission;
import com.vg.task.domain.model.SubmissionFile;
import com.vg.task.domain.model.Task;
import com.vg.task.exception.NotFoundException;
import com.vg.task.repository.SubmissionFileRepository;
import com.vg.task.repository.SubmissionRepository;
import com.vg.task.repository.TaskRepository;
import com.vg.task.service.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final SubmissionFileRepository submissionFileRepository;
    private final TaskRepository taskRepository;
    private final StudentClient studentClient;

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
                .flatMap(task -> studentClient.validateStudent(request.studentId())
                        .flatMap(valid -> {
                            if (!valid) return Mono.error(new NotFoundException("Student not found"));
                            
                            OffsetDateTime now = OffsetDateTime.now();
                            String status = now.isAfter(task.getDueDate()) ? "late" : "submitted";
                            
                            Submission submission = Submission.builder()
                                    .taskId(request.taskId())
                                    .studentId(request.studentId())
                                    .submissionDate(now)
                                    .status(status)
                                    .justificationReason(request.justificationReason())
                                    .createdAt(now)
                                    .updatedAt(now)
                                    .build();
                            
                            return submissionRepository.save(submission)
                                    .flatMap(saved -> {
                                        if (request.files() != null && !request.files().isEmpty()) {
                                            var files = new ArrayList<SubmissionFile>();
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
                        }))
                .flatMap(this::toResponse);
    }

    @Override
    public Mono<SubmissionResponseDTO> grade(Long id, GradeRequestDTO request) {
        return submissionRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Submission not found")))
                .flatMap(sub -> {
                    sub.setGrade(request.grade());
                    sub.setFeedback(request.feedback());
                    sub.setGradedBy(request.gradedBy());
                    sub.setGradedAt(OffsetDateTime.now());
                    sub.setStatus("graded");
                    sub.setUpdatedAt(OffsetDateTime.now());
                    return submissionRepository.save(sub);
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
        return submissionFileRepository.findBySubmissionId(sub.getId())
                .map(f -> new SubmissionFileDTO(f.getId(), f.getFileName(), f.getFileUrl(), f.getFileType(), f.getFileSizeKb()))
                .collectList()
                .map(files -> new SubmissionResponseDTO(
                        sub.getId(), sub.getTaskId(), sub.getStudentId(),
                        sub.getSubmissionDate(), sub.getStatus(), sub.getGrade(),
                        sub.getFeedback(), sub.getGradedBy(), sub.getGradedAt(),
                        sub.getJustificationReason(), sub.getCreatedAt(), sub.getUpdatedAt(), files
                ));
    }
}
