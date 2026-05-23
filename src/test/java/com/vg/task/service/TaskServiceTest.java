package com.vg.task.service;

import com.vg.task.domain.model.Task;
import com.vg.task.repository.TaskRepository;
import com.vg.task.service.impl.TaskServiceImpl;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Test
    void findAll_ShouldReturnOnlyNonDeletedTasks() {
        Task activeTask = Task.builder()
                .id(1L)
                .title("Activa")
                .isDeleted(false)
                .build();
        
        Task deletedTask = Task.builder()
                .id(2L)
                .title("Eliminada")
                .isDeleted(true)
                .build();

        when(taskRepository.findAll()).thenReturn(Flux.just(activeTask, deletedTask));

        StepVerifier.create(taskService.findAll())
                .expectNext(activeTask)
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void findById_WhenTaskExists_ShouldReturnTask() {
        Task task = Task.builder()
                .id(1L)
                .title("Tarea test")
                .isDeleted(false)
                .build();

        when(taskRepository.findById(1L)).thenReturn(Mono.just(task));

        StepVerifier.create(taskService.findById(1L))
                .expectNext(task)
                .verifyComplete();
    }

    @Test
    void findById_WhenTaskDeleted_ShouldReturnError() {
        Task deletedTask = Task.builder()
                .id(1L)
                .title("Eliminada")
                .isDeleted(true)
                .build();

        when(taskRepository.findById(1L)).thenReturn(Mono.just(deletedTask));

        StepVerifier.create(taskService.findById(1L))
                .expectError()
                .verify();
    }

    @Test
    void findAllPaged_ShouldReturnCorrectPage() {
        Task task1 = Task.builder().id(1L).title("Tarea 1").isDeleted(false).build();
        Task task2 = Task.builder().id(2L).title("Tarea 2").isDeleted(false).build();

        when(taskRepository.countActiveTasks()).thenReturn(Mono.just(2L));
        when(taskRepository.findAllPaged(0, 10)).thenReturn(Flux.just(task1, task2));

        StepVerifier.create(taskService.findAllPaged(0, 10))
                .expectNextMatches(page -> 
                    page.totalElements() == 2 && 
                    page.content().size() == 2)
                .verifyComplete();
    }
}
