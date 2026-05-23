package com.vg.task.service.impl;

import com.vg.task.application.port.input.SubmissionUseCase;
import com.vg.task.application.port.output.SubmissionRepositoryPort;
import com.vg.task.application.port.output.StudentServicePort;
import com.vg.task.domain.model.Submission;
import com.vg.task.domain.dto.GradeRequestDTO;
import com.vg.task.domain.dto.RubricGradeRequestDTO;
import com.vg.task.domain.dto.PageResponseDTO;
import com.vg.task.domain.dto.excel.ExcelGradeRowDTO;
import com.vg.task.exception.BadRequestException;
import com.vg.task.exception.NotFoundException;
import com.vg.task.repository.SubmissionGradeLogRepository;
import com.vg.task.service.ExcelProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
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
    private final ExcelProcessingService excelProcessingService;
    private final SubmissionGradeLogRepository gradeLogRepository;
    
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
                    submission.setSubmissionDate(OffsetDateTime.now());
                    submission.setStatus("submitted");
                    submission.setPresented(false);
                    submission.setIsLate(false);
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
                    Double oldGrade = submission.getGrade();
                    
                    submission.setGrade(request.grade());
                    submission.setFeedback(request.feedback());
                    submission.setGradedBy(request.gradedBy());
                    submission.setGradedAt(OffsetDateTime.now());
                    submission.setStatus("graded");
                    submission.setPresented(request.presented() != null ? request.presented() : true);
                    submission.setIsLate(request.isLate() != null ? request.isLate() : false);
                    submission.setObservations(request.observations());
                    
                    if (request.isLate() != null && request.isLate() && request.justification() != null) {
                        submission.setJustificationReason(request.justification());
                        submission.setJustifiedAt(OffsetDateTime.now());
                        submission.setJustifiedBy(request.gradedBy());
                    }
                    
                    if (request.presented() != null && request.presented()) {
                        submission.setPresentedAt(OffsetDateTime.now());
                    }
                    
                    submission.setUpdatedAt(OffsetDateTime.now());
                    
                    if (oldGrade != null && !oldGrade.equals(request.grade())) {
                        return gradeLogRepository.saveLog(id, oldGrade, request.grade(), request.gradedBy(), request.justification())
                            .then(submissionRepository.save(submission));
                    }
                    
                    return submissionRepository.save(submission);
                });
    }
    
    @Transactional
    public Mono<List<Submission>> bulkGrade(MultipartFile file, Integer gradedBy, Long taskId) {
        return excelProcessingService.processExcel(file)
            .flatMap(result -> {
                List<ExcelGradeRowDTO> validRows = result.validRows();
                if (validRows.isEmpty()) {
                    return Mono.error(new BadRequestException("No hay filas válidas en el Excel. Errores: " + result.errors().size()));
                }
                return Flux.fromIterable(validRows)
                    .flatMap(row -> processGradeRow(row, gradedBy, taskId))
                    .collectList();
            })
            .doOnSuccess(list -> log.info("✅ Procesadas {} calificaciones masivas", list.size()));
    }
    
    private Mono<Submission> processGradeRow(ExcelGradeRowDTO row, Integer gradedBy, Long taskId) {
        Long finalTaskId = row.getTaskId() != null ? row.getTaskId() : taskId;
        if (finalTaskId == null) {
            return Mono.error(new BadRequestException("Task ID es requerido en cada fila o en el request"));
        }
        
        return submissionRepository.findByTaskIdAndStudentId(finalTaskId, row.getStudentId())
            .switchIfEmpty(createNewSubmission(finalTaskId, row.getStudentId()))
            .flatMap(submission -> {
                Double oldGrade = submission.getGrade();
                
                submission.setGrade(row.getGrade());
                submission.setGradedBy(gradedBy);
                submission.setGradedAt(OffsetDateTime.now());
                submission.setStatus("graded");
                submission.setPresented(row.getGrade() != null && row.getGrade() > 0);
                submission.setIsLate(row.getIsLate());
                submission.setObservations(row.getObservations());
                
                if (row.getIsLate() != null && row.getIsLate() && row.getJustification() != null) {
                    submission.setJustificationReason(row.getJustification());
                    submission.setJustifiedAt(OffsetDateTime.now());
                    submission.setJustifiedBy(gradedBy);
                }
                
                if (submission.getPresented() != null && submission.getPresented()) {
                    submission.setPresentedAt(OffsetDateTime.now());
                }
                
                submission.setUpdatedAt(OffsetDateTime.now());
                
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
        return Mono.error(new UnsupportedOperationException("Reintentos no soportados en entregas físicas"));
    }
    
    @Override
    public Mono<Submission> excuse(Long id, String reason) {
        return submissionRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Submission not found: " + id)))
                .flatMap(submission -> {
                    submission.setStatus("excused");
                    submission.setJustificationReason(reason);
                    submission.setJustifiedAt(OffsetDateTime.now());
                    submission.setUpdatedAt(OffsetDateTime.now());
                    return submissionRepository.save(submission);
                });
    }
    
    @Override
    public Mono<Void> delete(Long id) {
        return submissionRepository.deleteById(id);
    }
}
