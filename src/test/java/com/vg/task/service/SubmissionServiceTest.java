package com.vg.task.service;

import com.vg.task.application.port.output.StudentServicePort;
import com.vg.task.application.port.output.SubmissionRepositoryPort;
import com.vg.task.domain.model.Submission;
import com.vg.task.domain.model.Task;
import com.vg.task.domain.dto.GradeRequestDTO;
import com.vg.task.repository.SubmissionGradeLogRepository;
import com.vg.task.repository.TaskRepository;
import com.vg.task.service.impl.SubmissionServiceImpl;
import com.vg.task.service.ExcelProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SubmissionRepositoryPort submissionRepository;

    @Mock
    private StudentServicePort studentService;

    @Mock
    private ExcelProcessingService excelProcessingService;

    @Mock
    private SubmissionGradeLogRepository gradeLogRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private SubmissionServiceImpl submissionService;

    private Submission validSubmission;

    @BeforeEach
    void setUp() {
        validSubmission = Submission.builder()
                .id(1L)
                .taskId(1L)
                .studentId(100)
                .status("submitted")
                .submissionDate(OffsetDateTime.now())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void findById_ShouldReturnSubmission_WhenExists() {
        when(submissionRepository.findById(1L)).thenReturn(Mono.just(validSubmission));

        StepVerifier.create(submissionService.findById(1L))
                .expectNext(validSubmission)
                .verifyComplete();
    }

    @Test
    void findById_ShouldReturnError_WhenNotExists() {
        when(submissionRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(submissionService.findById(999L))
                .expectError()
                .verify();
    }

    @Test
    void submit_ShouldSaveSubmission_WhenStudentIsValid() {
        Submission newSubmission = Submission.builder()
                .taskId(1L)
                .studentId(100)
                .build();

        when(studentService.validateStudent(100)).thenReturn(Mono.just(true));
        when(submissionRepository.findByTaskIdAndStudentId(1L, 100)).thenReturn(Mono.empty());
        when(submissionRepository.save(any(Submission.class))).thenReturn(Mono.just(validSubmission));

        StepVerifier.create(submissionService.submit(newSubmission))
                .expectNext(validSubmission)
                .verifyComplete();
    }

    @Test
    void submit_ShouldReturnError_WhenStudentIsInvalid() {
        Submission newSubmission = Submission.builder()
                .taskId(1L)
                .studentId(999)
                .build();

        when(studentService.validateStudent(999)).thenReturn(Mono.just(false));

        StepVerifier.create(submissionService.submit(newSubmission))
                .expectError()
                .verify();
    }
}
