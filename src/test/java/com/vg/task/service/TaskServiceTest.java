package com.vg.task.service;

import com.vg.task.domain.model.Task;
import com.vg.task.repository.TaskRepository;
import com.vg.task.service.impl.TaskServiceImpl;
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
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task task;

    @BeforeEach
    void setUp() {
        task = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Description")
                .classId(1)
                .dueDate(OffsetDateTime.now().plusDays(7))
                .status("draft")
                .isDeleted(false)
                .createdBy(1)
                .build();
    }

    @Test
    void findAll_ShouldReturnAllNonDeletedTasks() {
        when(taskRepository.findAll()).thenReturn(Flux.just(task));
        
        StepVerifier.create(taskService.findAll())
                .expectNextMatches(t -> !t.getIsDeleted())
                .verifyComplete();
    }

    @Test
    void findById_ShouldReturnTask_WhenExists() {
        when(taskRepository.findById(1L)).thenReturn(Mono.just(task));
        
        StepVerifier.create(taskService.findById(1L))
                .expectNextMatches(t -> t.getId().equals(1L))
                .verifyComplete();
    }

    @Test
    void save_ShouldSaveTask_WhenValid() {
        when(taskRepository.save(any(Task.class))).thenReturn(Mono.just(task));
        
        StepVerifier.create(taskService.save(task))
                .expectNextMatches(t -> t.getTitle().equals("Test Task"))
                .verifyComplete();
    }
}
