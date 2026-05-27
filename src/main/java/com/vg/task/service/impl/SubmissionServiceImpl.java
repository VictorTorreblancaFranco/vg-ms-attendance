package com.vg.task.service.impl;

import com.vg.task.service.SubmissionService;
import com.vg.task.service.port.SubmissionRepositoryPort;
import com.vg.task.service.port.StudentServicePort;
import com.vg.task.domain.model.Submission;
import com.vg.task.domain.model.Task;
import com.vg.task.domain.dto.GradeRequestDTO;
import com.vg.task.domain.dto.RubricGradeRequestDTO;
import com.vg.task.domain.dto.SubmissionRequestDTO;
import com.vg.task.domain.dto.SubmissionResponseDTO;
import com.vg.task.domain.dto.PageResponseDTO;
import com.vg.task.domain.dto.excel.ExcelGradeRowDTO;
import com.vg.task.domain.model.exceptions.BadRequestException;
import com.vg.task.domain.model.exceptions.NotFoundException;
import com.vg.task.mapper.SubmissionMapper;
import com.vg.task.repository.SubmissionGradeLogRepository;
import com.vg.task.repository.TaskRepository;
import com.vg.task.client.AcademicClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionService {
    
    private final SubmissionRepositoryPort submissionRepository;
    private final StudentServicePort studentService;
    private final SubmissionGradeLogRepository gradeLogRepository;
    private final TaskRepository taskRepository;
    private final AcademicClient academicClient;
    private final SubmissionMapper mapper;
    
    @Value("${submission.grace-period-days:30}")
    private int gracePeriodDays;
    
    @Override
    public Flux<SubmissionResponseDTO> findAll() {
        return submissionRepository.findAll().map(mapper::toResponse);
    }
    
    public Mono<PageResponseDTO<SubmissionResponseDTO>> findAllPaged(int page, int size) {
        return submissionRepository.findAll()
                .collectList()
                .flatMap(list -> {
                    int total = list.size();
                    int start = page * size;
                    int end = Math.min(start + size, total);
                    if (start >= total) {
                        return Mono.just(PageResponseDTO.of(new ArrayList<>(), page, size, total));
                    }
                    List<SubmissionResponseDTO> paged = list.subList(start, end).stream()
                            .map(mapper::toResponse).toList();
                    return Mono.just(PageResponseDTO.of(paged, page, size, total));
                });
    }
    
    @Override
    public Flux<SubmissionResponseDTO> findByTaskId(Long taskId) {
        return submissionRepository.findByTaskId(taskId).map(mapper::toResponse);
    }
    
    public Mono<PageResponseDTO<SubmissionResponseDTO>> findByTaskIdPaged(Long taskId, int page, int size) {
        return submissionRepository.countByTaskId(taskId)
            .flatMap(total -> {
                if (total == 0) {
                    return Mono.just(PageResponseDTO.of(new ArrayList<>(), page, size, total));
                }
                return submissionRepository.findByTaskId(taskId, page, size)
                    .collectList()
                    .map(submissions -> PageResponseDTO.of(
                            submissions.stream().map(mapper::toResponse).toList(),
                            page, size, total));
            });
    }
    
    @Override
    public Flux<SubmissionResponseDTO> findByStudentId(Integer studentId) {
        return submissionRepository.findByStudentId(studentId).map(mapper::toResponse);
    }
    
    public Mono<PageResponseDTO<SubmissionResponseDTO>> findByStudentIdPaged(Integer studentId, int page, int size) {
        return submissionRepository.countByStudentId(studentId)
            .flatMap(total -> {
                if (total == 0) {
                    return Mono.just(PageResponseDTO.of(new ArrayList<>(), page, size, total));
                }
                return submissionRepository.findByStudentId(studentId, page, size)
                    .collectList()
                    .map(submissions -> PageResponseDTO.of(
                            submissions.stream().map(mapper::toResponse).toList(),
                            page, size, total));
            });
    }
    
    @Override
    public Mono<SubmissionResponseDTO> findById(Long id) {
        return submissionRepository.findById(id)
                .map(mapper::toResponse)
                .switchIfEmpty(Mono.error(new NotFoundException("Submission not found: " + id)));
    }
    
    @Override
    public Mono<SubmissionResponseDTO> submit(SubmissionRequestDTO request) {
        Submission submission = mapper.toDomain(request);
        return studentService.validateStudent(submission.getStudentId())
            .flatMap(valid -> {
                if (!valid) {
                    return Mono.error(new NotFoundException("Student not found: " + submission.getStudentId()));
                }
                return submissionRepository.findByTaskIdAndStudentId(submission.getTaskId(), submission.getStudentId())
                    .flatMap(existing -> {
                        existing.setJustificationReason(submission.getJustificationReason());
                        existing.setUpdatedAt(OffsetDateTime.now());
                        return submissionRepository.save(existing);
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        submission.setSubmissionDate(OffsetDateTime.now());
                        submission.setStatus("submitted");
                        submission.setPresented(false);
                        submission.setIsLate(false);
                        submission.setCreatedAt(OffsetDateTime.now());
                        submission.setUpdatedAt(OffsetDateTime.now());
                        return submissionRepository.save(submission);
                    }));
            }).map(mapper::toResponse);
    }
    
    @Override
    public Mono<SubmissionResponseDTO> grade(Long id, GradeRequestDTO request) {
        return submissionRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Submission not found: " + id)))
                .flatMap(submission -> 
                    validateTaskGradingPeriod(submission.getTaskId())
                        .then(validateGradeRange(request.grade()))
                        .then(validateStudentBelongsToTask(submission.getTaskId(), submission.getStudentId()))
                        .then(Mono.defer(() -> {
                            Double oldGrade = submission.getGrade();
                            OffsetDateTime now = OffsetDateTime.now();
                            
                            submission.setGrade(request.grade());
                            submission.setFeedback(request.feedback());
                            submission.setGradedBy(request.gradedBy());
                            submission.setGradedAt(now);
                            submission.setStatus("graded");
                            submission.setPresented(request.presented() != null ? request.presented() : true);
                            submission.setIsLate(request.isLate() != null ? request.isLate() : false);
                            submission.setObservations(request.observations());
                            
                            if (request.isLate() != null && request.isLate() && request.justification() != null) {
                                submission.setJustificationReason(request.justification());
                                submission.setJustifiedAt(now);
                                submission.setJustifiedBy(request.gradedBy());
                            }
                            
                            if (request.presented() != null && request.presented()) {
                                submission.setPresentedAt(now);
                            }
                            
                            submission.setUpdatedAt(now);
                            
                            if (oldGrade != null && !oldGrade.equals(request.grade())) {
                                return gradeLogRepository.saveLog(id, oldGrade, request.grade(), request.gradedBy(), request.justification())
                                    .then(submissionRepository.save(submission));
                            }
                            return submissionRepository.save(submission);
                        }))
                ).map(mapper::toResponse);
    }
    
    private Mono<Void> validateStudentBelongsToTask(Long taskId, Integer studentId) {
        return taskRepository.findById(taskId)
            .switchIfEmpty(Mono.error(new NotFoundException("Task not found: " + taskId)))
            .flatMap(task -> academicClient.getClassInfo(task.getClassId())
                .switchIfEmpty(Mono.error(new NotFoundException("Class not found: " + task.getClassId())))
                .flatMap(classInfo -> {
                    Integer gradoId = classInfo.gradoId();
                    return studentService.getStudentInfo(studentId)
                        .flatMap(studentInfo -> {
                            if (!studentInfo.gradeId().equals(gradoId)) {
                                return Mono.error(new BadRequestException(
                                    "El estudiante no pertenece al grado de esta tarea. " +
                                    "Grado de la tarea: " + gradoId + ", Grado del estudiante: " + studentInfo.gradeId()
                                ));
                            }
                            return Mono.empty();
                        });
                })
            );
    }
    
    private Mono<Void> validateTaskGradingPeriod(Long taskId) {
        return taskRepository.findById(taskId)
            .switchIfEmpty(Mono.error(new NotFoundException("Task not found: " + taskId)))
            .flatMap(task -> {
                OffsetDateTime now = OffsetDateTime.now();
                OffsetDateTime dueDate = task.getDueDate();
                String taskStatus = task.getStatus();
                
                if ("closed".equals(taskStatus) || "archived".equals(taskStatus)) {
                    return Mono.error(new BadRequestException("No se puede calificar: La tarea está " + taskStatus));
                }
                
                if (now.isAfter(dueDate)) {
                    long daysLate = ChronoUnit.DAYS.between(dueDate, now);
                    if (daysLate > gracePeriodDays) {
                        return Mono.error(new BadRequestException(
                            String.format("Período de gracia expirado: %d días de retraso", daysLate)
                        ));
                    }
                    log.warn("⚠️ Calificando con {} días de retraso", daysLate);
                }
                return Mono.empty();
            });
    }
    
    private Mono<Void> validateGradeRange(Double grade) {
        if (grade == null || grade < 0 || grade > 20) {
            return Mono.error(new BadRequestException("La nota debe estar entre 0 y 20"));
        }
        return Mono.empty();
    }
    
    @Override
    public Mono<SubmissionResponseDTO> gradeWithRubric(Long id, RubricGradeRequestDTO request) {
        return submissionRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Submission not found: " + id)))
                .flatMap(submission -> 
                    validateTaskGradingPeriod(submission.getTaskId())
                        .then(Mono.defer(() -> {
                            Double totalScore = request.scores().stream()
                                    .mapToDouble(item -> item.score())
                                    .sum();
                            submission.setGrade(totalScore);
                            submission.setGradedBy(request.gradedBy());
                            submission.setGradedAt(OffsetDateTime.now());
                            submission.setStatus("graded");
                            submission.setUpdatedAt(OffsetDateTime.now());
                            return submissionRepository.save(submission);
                        }))
                ).map(mapper::toResponse);
    }
    
    @Override
    public Mono<SubmissionResponseDTO> allowReattempt(Long id, Integer maxAttempts) {
        return Mono.error(new UnsupportedOperationException("Reintentos no soportados"));
    }
    
    @Override
    public Mono<SubmissionResponseDTO> excuse(Long id, String reason) {
        return submissionRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Submission not found: " + id)))
                .flatMap(submission -> 
                    validateTaskGradingPeriod(submission.getTaskId())
                        .then(Mono.defer(() -> {
                            submission.setStatus("excused");
                            submission.setJustificationReason(reason);
                            submission.setJustifiedAt(OffsetDateTime.now());
                            submission.setUpdatedAt(OffsetDateTime.now());
                            return submissionRepository.save(submission);
                        }))
                ).map(mapper::toResponse);
    }
    
    @Override
    public Mono<Void> delete(Long id) {
        return submissionRepository.deleteById(id);
    }
}
