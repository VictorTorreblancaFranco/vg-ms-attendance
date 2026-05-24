package com.vg.task.service.impl;

import com.vg.task.service.port.SubmissionUseCase;
import com.vg.task.service.port.SubmissionRepositoryPort;
import com.vg.task.service.port.StudentServicePort;
import com.vg.task.domain.model.Submission;
import com.vg.task.domain.model.Task;
import com.vg.task.domain.dto.GradeRequestDTO;
import com.vg.task.domain.dto.RubricGradeRequestDTO;
import com.vg.task.domain.dto.PageResponseDTO;
import com.vg.task.domain.dto.excel.ExcelGradeRowDTO;
import com.vg.task.exception.BadRequestException;
import com.vg.task.exception.NotFoundException;
import com.vg.task.repository.SubmissionGradeLogRepository;
import com.vg.task.repository.TaskRepository;
import com.vg.task.service.impl.ExcelProcessingService;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionServiceImpl implements SubmissionUseCase {
    
    private final SubmissionRepositoryPort submissionRepository;
    private final StudentServicePort studentService;
    private final ExcelProcessingService excelProcessingService;
    private final SubmissionGradeLogRepository gradeLogRepository;
    private final TaskRepository taskRepository;
    
    @Value("${submission.grace-period-days:30}")
    private int gracePeriodDays;
    
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
                
                return submissionRepository.findByTaskIdAndStudentId(
                        submission.getTaskId(), submission.getStudentId())
                    .flatMap(existing -> {
                        log.info("📝 Actualizando registro para tarea {} estudiante {}", 
                                submission.getTaskId(), submission.getStudentId());
                        existing.setJustificationReason(submission.getJustificationReason());
                        existing.setUpdatedAt(OffsetDateTime.now());
                        return submissionRepository.save(existing);
                    })
                    .switchIfEmpty(
                        Mono.defer(() -> {
                            log.info("📝 Creando nuevo registro para tarea {} estudiante {}", 
                                    submission.getTaskId(), submission.getStudentId());
                            submission.setSubmissionDate(OffsetDateTime.now());
                            submission.setStatus("submitted");
                            submission.setPresented(false);
                            submission.setIsLate(false);
                            submission.setCreatedAt(OffsetDateTime.now());
                            submission.setUpdatedAt(OffsetDateTime.now());
                            return submissionRepository.save(submission);
                        })
                    );
            });
    }
    
    @Override
    public Mono<Submission> grade(Long id, GradeRequestDTO request) {
        return submissionRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Submission not found: " + id)))
                .flatMap(submission -> 
                    validateTaskGradingPeriod(submission.getTaskId())
                        .then(validateGradeRange(request.grade()))
                        .then(validateLateWithJustification(request))
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
                );
    }
    
    private Mono<Void> validateTaskGradingPeriod(Long taskId) {
        return taskRepository.findById(taskId)
            .switchIfEmpty(Mono.error(new NotFoundException("Task not found: " + taskId)))
            .flatMap(task -> {
                OffsetDateTime now = OffsetDateTime.now();
                OffsetDateTime dueDate = task.getDueDate();
                String taskStatus = task.getStatus();
                
                if ("closed".equals(taskStatus)) {
                    return Mono.error(new BadRequestException(
                        "No se puede calificar: La tarea '" + task.getTitle() + "' está cerrada."
                    ));
                }
                
                if ("archived".equals(taskStatus)) {
                    return Mono.error(new BadRequestException(
                        "No se puede calificar: La tarea '" + task.getTitle() + "' está archivada."
                    ));
                }
                
                if (now.isAfter(dueDate)) {
                    long daysLate = ChronoUnit.DAYS.between(dueDate, now);
                    
                    if (daysLate > gracePeriodDays) {
                        return Mono.error(new BadRequestException(
                            String.format("No se puede calificar: La fecha límite fue hace %d días. Período de gracia es de %d días.", daysLate, gracePeriodDays)
                        ));
                    }
                    
                    log.warn("⚠️ Calificando tarea {} con {} días de retraso (dentro del período de gracia)", taskId, daysLate);
                }
                
                return Mono.empty();
            });
    }
    
    private Mono<Void> validateGradeRange(Double grade) {
        if (grade == null) {
            return Mono.error(new BadRequestException("La nota es requerida"));
        }
        if (grade < 0 || grade > 20) {
            return Mono.error(new BadRequestException("La nota debe estar entre 0 y 20"));
        }
        return Mono.empty();
    }
    
    private Mono<Void> validateLateWithJustification(GradeRequestDTO request) {
        if (request.isLate() != null && request.isLate()) {
            if (request.justification() == null || request.justification().isBlank()) {
                return Mono.error(new BadRequestException(
                    "Debe proporcionar una justificación cuando la entrega es tardía"
                ));
            }
            if (request.justification().length() < 10) {
                return Mono.error(new BadRequestException(
                    "La justificación debe tener al menos 10 caracteres"
                ));
            }
        }
        return Mono.empty();
    }
    
    @Override
    @Transactional
    public Mono<List<Submission>> bulkGrade(MultipartFile file, Integer gradedBy, Long taskId) {
        log.info("📊 Iniciando carga masiva con transacción - gradedBy: {}, taskId: {}", gradedBy, taskId);
        
        return excelProcessingService.processExcel(file)
            .flatMap(result -> {
                List<ExcelGradeRowDTO> validRows = result.validRows();
                List<ExcelProcessingService.ExcelError> errors = result.errors();
                
                if (validRows.isEmpty()) {
                    return Mono.error(new BadRequestException(
                        "No hay filas válidas en el Excel. " + errors.size() + " errores encontrados."
                    ));
                }
                
                Set<String> seen = new HashSet<>();
                List<String> duplicates = new ArrayList<>();
                for (ExcelGradeRowDTO row : validRows) {
                    String key = (row.getTaskId() != null ? row.getTaskId() : taskId) + "|" + row.getStudentId();
                    if (seen.contains(key)) {
                        duplicates.add("Tarea " + (row.getTaskId() != null ? row.getTaskId() : taskId) + " - Estudiante " + row.getStudentId());
                    }
                    seen.add(key);
                }
                
                if (!duplicates.isEmpty()) {
                    return Mono.error(new BadRequestException("Duplicados encontrados en el Excel: " + String.join(", ", duplicates)));
                }
                
                Long finalTaskId = validRows.get(0).getTaskId() != null ? validRows.get(0).getTaskId() : taskId;
                return validateTaskGradingPeriod(finalTaskId)
                    .thenMany(Flux.fromIterable(validRows)
                        .concatMap(row -> {
                            Long rowTaskId = row.getTaskId() != null ? row.getTaskId() : taskId;
                            if (row.getGrade() != null && (row.getGrade() < 0 || row.getGrade() > 20)) {
                                return Mono.error(new BadRequestException(
                                    "Nota inválida para estudiante " + row.getStudentId() + ": " + row.getGrade()
                                ));
                            }
                            if (row.getIsLate() != null && row.getIsLate() && 
                                (row.getJustification() == null || row.getJustification().isBlank())) {
                                return Mono.error(new BadRequestException(
                                    "Justificación requerida para estudiante " + row.getStudentId()
                                ));
                            }
                            return processGradeRow(row, gradedBy, rowTaskId);
                        }))
                    .collectList()
                    .map(list -> {
                        log.info("✅ Transacción completada: {} registros guardados", list.size());
                        return list;
                    });
            });
    }
    
    private Mono<Submission> processGradeRow(ExcelGradeRowDTO row, Integer gradedBy, Long taskId) {
        Long finalTaskId = row.getTaskId() != null ? row.getTaskId() : taskId;
        
        return submissionRepository.findByTaskIdAndStudentId(finalTaskId, row.getStudentId())
            .switchIfEmpty(createNewSubmission(finalTaskId, row.getStudentId()))
            .flatMap(submission -> {
                Double oldGrade = submission.getGrade();
                OffsetDateTime now = OffsetDateTime.now();
                
                submission.setGrade(row.getGrade());
                submission.setGradedBy(gradedBy);
                submission.setGradedAt(now);
                submission.setStatus("graded");
                submission.setPresented(row.getGrade() != null && row.getGrade() > 0);
                submission.setIsLate(row.getIsLate() != null && row.getIsLate());
                submission.setObservations(row.getObservations());
                submission.setUpdatedAt(now);
                
                if (submission.getPresented() != null && submission.getPresented()) {
                    submission.setPresentedAt(now);
                }
                
                if (row.getIsLate() != null && row.getIsLate() && row.getJustification() != null) {
                    submission.setJustificationReason(row.getJustification());
                    submission.setJustifiedAt(now);
                    submission.setJustifiedBy(gradedBy);
                }
                
                if (oldGrade != null && !oldGrade.equals(row.getGrade())) {
                    return gradeLogRepository.saveLog(submission.getId(), oldGrade, row.getGrade(), gradedBy, row.getJustification())
                        .then(submissionRepository.save(submission));
                }
                
                return submissionRepository.save(submission);
            });
    }
    
    private Mono<Submission> createNewSubmission(Long taskId, Integer studentId) {
        Submission submission = Submission.builder()
                .taskId(taskId)
                .studentId(studentId)
                .submissionDate(OffsetDateTime.now())
                .status("submitted")
                .presented(false)
                .isLate(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        return submissionRepository.save(submission);
    }
    
    @Override
    public Mono<Submission> gradeWithRubric(Long id, RubricGradeRequestDTO request) {
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
                );
    }
    
    @Override
    public Mono<Submission> allowReattempt(Long id, Integer maxAttempts) {
        return Mono.error(new UnsupportedOperationException("Reintentos no soportados en entregas físicas"));
    }
    
    @Override
    public Mono<Submission> excuse(Long id, String reason) {
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
                );
    }
    
    @Override
    public Mono<Void> delete(Long id) {
        return submissionRepository.deleteById(id);
    }
}
