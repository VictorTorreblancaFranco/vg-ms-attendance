package com.vg.task.service.impl;

import com.vg.task.client.StudentClient;
import com.vg.task.domain.dto.GradeSubmissionRequestDTO;
import com.vg.task.domain.dto.TaskSubmissionRequestDTO;
import com.vg.task.domain.dto.TaskSubmissionResponseDTO;
import com.vg.task.domain.model.Task;
import com.vg.task.domain.model.TaskSubmission;
import com.vg.task.exception.NotFoundException;
import com.vg.task.repository.TaskRepository;
import com.vg.task.repository.TaskSubmissionRepository;
import com.vg.task.service.TaskSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class TaskSubmissionServiceImpl implements TaskSubmissionService {

    private final TaskSubmissionRepository taskSubmissionRepository;
    private final TaskRepository taskRepository;
    private final StudentClient studentClient;

    @Override
    public Flux<TaskSubmissionResponseDTO> findAll() {
        return taskSubmissionRepository.findAll()
                .map(this::toResponseDTO);
    }

    @Override
    public Mono<TaskSubmissionResponseDTO> findById(Long id) {
        return taskSubmissionRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Submission not found with id: " + id)))
                .map(this::toResponseDTO);
    }

    @Override
    public Mono<TaskSubmissionResponseDTO> submit(TaskSubmissionRequestDTO request, Integer userId) {
        return taskRepository.findById(request.taskId())
                .switchIfEmpty(Mono.error(new NotFoundException("Task not found with id: " + request.taskId())))
                .flatMap(task -> studentClient.validateStudent(request.studentId())
                    .flatMap(isValid -> {
                        if (!isValid) {
                            return Mono.error(new NotFoundException("Student not found with id: " + request.studentId()));
                        }
                        return taskSubmissionRepository.countByTaskIdAndStudentId(request.taskId(), request.studentId())
                            .flatMap(attemptCount -> {
                                short nextAttempt = (short)(attemptCount + 1);
                                
                                TaskSubmission submission = TaskSubmission.builder()
                                    .taskId(request.taskId())
                                    .studentId(request.studentId())
                                    .attemptNumber(nextAttempt)
                                    .submissionDate(OffsetDateTime.now())
                                    .status("submitted")
                                    .studentComment(request.studentComment())
                                    .createdAt(OffsetDateTime.now())
                                    .updatedAt(OffsetDateTime.now())
                                    .build();
                                
                                return taskSubmissionRepository.save(submission);
                            });
                    }))
                .map(this::toResponseDTO);
    }

    @Override
    public Mono<TaskSubmissionResponseDTO> grade(Long id, GradeSubmissionRequestDTO request) {
        return taskSubmissionRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Submission not found with id: " + id)))
                .flatMap(submission -> {
                    submission.setGrade(request.grade());
                    submission.setFeedback(request.feedback());
                    submission.setGradedBy(request.gradedBy());
                    submission.setGradedAt(OffsetDateTime.now());
                    submission.setStatus("graded");
                    submission.setUpdatedAt(OffsetDateTime.now());
                    return taskSubmissionRepository.save(submission);
                })
                .map(this::toResponseDTO);
    }

    @Override
    public Flux<TaskSubmissionResponseDTO> findByTaskId(Long taskId) {
        return taskSubmissionRepository.findByTaskId(taskId)
                .map(this::toResponseDTO);
    }

    @Override
    public Flux<TaskSubmissionResponseDTO> findByStudentId(Integer studentId) {
        return taskSubmissionRepository.findByStudentId(studentId)
                .map(this::toResponseDTO);
    }

    @Override
    public Mono<Void> delete(Long id) {
        return taskSubmissionRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Submission not found with id: " + id)))
                .flatMap(taskSubmissionRepository::delete);
    }

    private TaskSubmissionResponseDTO toResponseDTO(TaskSubmission submission) {
        return new TaskSubmissionResponseDTO(
            submission.getId(),
            submission.getTaskId(),
            submission.getStudentId(),
            submission.getAttemptNumber(),
            submission.getSubmissionDate(),
            submission.getStatus(),
            submission.getGrade(),
            submission.getFeedback(),
            submission.getGradedBy(),
            submission.getGradedAt(),
            submission.getStudentComment(),
            submission.getCreatedAt(),
            submission.getUpdatedAt()
        );
    }
}
