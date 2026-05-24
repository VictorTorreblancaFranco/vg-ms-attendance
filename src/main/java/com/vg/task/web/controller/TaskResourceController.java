package com.vg.task.web.controller;

import com.vg.task.domain.model.TaskResource;
import com.vg.task.repository.TaskResourceRepository;
import com.vg.task.service.impl.CloudinaryUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/task")
@RequiredArgsConstructor
public class TaskResourceController {
    private final TaskResourceRepository resourceRepository;
    private final CloudinaryUploadService cloudinaryService;

    @PostMapping("/{taskId}/resources")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TaskResource> uploadResource(@PathVariable Long taskId, @RequestPart("file") FilePart file,
                                             @RequestParam(defaultValue = "recurso") String title,
                                             @RequestParam(defaultValue = "file") String type) {
        return cloudinaryService.upload(file, "task_resources")
                .flatMap(url -> {
                    TaskResource resource = TaskResource.builder()
                            .taskId(taskId).type(type).name(title).url(url)
                            .createdAt(OffsetDateTime.now()).build();
                    return resourceRepository.save(resource);
                });
    }

    @PostMapping("/{taskId}/links")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TaskResource> addLink(@PathVariable Long taskId, @RequestBody Map<String, String> body) {
        TaskResource resource = TaskResource.builder()
                .taskId(taskId).type("link").name(body.get("title")).url(body.get("url"))
                .createdAt(OffsetDateTime.now()).build();
        return resourceRepository.save(resource);
    }

    @GetMapping("/{taskId}/resources")
    public Flux<TaskResource> getByTask(@PathVariable Long taskId) {
        return resourceRepository.findByTaskId(taskId);
    }
}
