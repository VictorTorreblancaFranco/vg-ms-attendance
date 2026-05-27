package com.vg.task.scheduler;

import com.vg.task.repository.TaskRepository;
import com.vg.task.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class ScheduledTaskProcessor {

    private final TaskRepository taskRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedDelay = 60000)
    public void processScheduledTasks() {
        OffsetDateTime now = OffsetDateTime.now();
        
        // Publicar tareas programadas
        taskRepository.findByScheduledPublishDateBeforeAndStatusAndIsDeletedFalse(now, "draft")
                .doOnNext(task -> log.info("Auto-publishing task: {}", task.getId()))
                .flatMap(task -> {
                    task.setStatus("published");
                    task.setUpdatedAt(now);
                    return taskRepository.save(task)
                            .flatMap(saved -> notificationService.send(
                                    saved.getCreatedBy(),
                                    saved.getId(),
                                    "TASK_PUBLISHED",
                                    "Tarea Publicada",
                                    "La tarea '" + saved.getTitle() + "' ya está disponible"
                            ).thenReturn(saved));
                })
                .subscribe();
        
        // Cerrar tareas programadas
        taskRepository.findByScheduledCloseDateBeforeAndStatusAndIsDeletedFalse(now, "published")
                .doOnNext(task -> log.info("Auto-closing task: {}", task.getId()))
                .flatMap(task -> {
                    task.setStatus("closed");
                    task.setUpdatedAt(now);
                    return taskRepository.save(task)
                            .flatMap(saved -> notificationService.send(
                                    saved.getCreatedBy(),
                                    saved.getId(),
                                    "TASK_CLOSED",
                                    "Tarea Cerrada",
                                    "La tarea '" + saved.getTitle() + "' ha sido cerrada"
                            ).thenReturn(saved));
                })
                .subscribe();
        
        // Notificar vencimiento 3 horas antes
        OffsetDateTime threeHoursFromNow = now.plusHours(3);
        taskRepository.findByDueDateBeforeAndStatusAndIsDeletedFalse(threeHoursFromNow, "published")
                .doOnNext(task -> log.info("Task will close soon: {}", task.getId()))
                .flatMap(task -> notificationService.send(
                        task.getCreatedBy(),
                        task.getId(),
                        "TASK_CLOSING_SOON",
                        "Tarea por vencer",
                        "La tarea '" + task.getTitle() + "' cierra en 3 horas"
                ))
                .subscribe();
    }
}
