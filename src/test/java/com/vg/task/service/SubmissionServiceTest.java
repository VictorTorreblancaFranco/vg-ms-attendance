package com.vg.task.service;

import com.vg.task.application.port.output.SubmissionRepositoryPort;
import com.vg.task.domain.model.Submission;
import com.vg.task.service.impl.SubmissionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {

    @Mock
    private SubmissionRepositoryPort submissionRepository;

    @InjectMocks
    private SubmissionServiceImpl submissionService;

    private Submission submission;

    @BeforeEach
    void setUp() {
        submission = Submission.builder()
                .id(1L)
                .taskId(1L)
                .studentId(3)
                .status("submitted")
                .presented(false)
                .isLate(false)
                .submissionDate(OffsetDateTime.now())
                .build();
    }

    @Test
    void findAll_ShouldReturnAllSubmissions() {
        when(submissionRepository.findAll()).thenReturn(Flux.just(submission));
        
        StepVerifier.create(submissionService.findAll())
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findById_ShouldReturnSubmission_WhenExists() {
        when(submissionRepository.findById(1L)).thenReturn(Mono.just(submission));
        
        StepVerifier.create(submissionService.findById(1L))
                .expectNextMatches(s -> s.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void submit_ShouldSaveSubmission_WhenValid() {
        when(submissionRepository.save(any(Submission.class))).thenReturn(Mono.just(submission));
        
        StepVerifier.create(submissionService.submit(submission))
                .expectNextMatches(s -> s.getStudentId().equals(3))
                .verifyComplete();
    }
}
