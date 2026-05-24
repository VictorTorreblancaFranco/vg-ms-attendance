package com.vg.task.web.handler;

import com.vg.task.domain.model.TaskResource;
import com.vg.task.repository.TaskResourceRepository;
import com.vg.task.service.impl.CloudinaryUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TaskResourceHandler {

    private final TaskResourceRepository resourceRepository;
    private final CloudinaryUploadService cloudinaryService;

    public Mono<ServerResponse> uploadResource(ServerRequest request) {
        Long taskId = Long.parseLong(request.pathVariable("taskId"));
        return request.multipartData()
            .flatMap(parts -> {
                FilePart file = (FilePart) parts.toSingleValueMap().get("file");
                String title = request.queryParam("title").orElse("recurso");
                String type = request.queryParam("type").orElse("file");
                
                return cloudinaryService.upload(file, "task_resources")
                    .flatMap(url -> {
                        TaskResource resource = TaskResource.builder()
                            .taskId(taskId)
                            .type(type)
                            .name(title)
                            .url(url)
                            .createdAt(OffsetDateTime.now())
                            .build();
                        return resourceRepository.save(resource);
                    });
            })
            .flatMap(res -> ServerResponse.ok().bodyValue(res));
    }

    public Mono<ServerResponse> addLink(ServerRequest request) {
        Long taskId = Long.parseLong(request.pathVariable("taskId"));
        return request.bodyToMono(Map.class)
            .flatMap(body -> {
                TaskResource resource = TaskResource.builder()
                    .taskId(taskId)
                    .type("link")
                    .name((String) body.get("title"))
                    .url((String) body.get("url"))
                    .createdAt(OffsetDateTime.now())
                    .build();
                return resourceRepository.save(resource);
            })
            .flatMap(res -> ServerResponse.ok().bodyValue(res));
    }

    public Mono<ServerResponse> getByTask(ServerRequest request) {
        Long taskId = Long.parseLong(request.pathVariable("taskId"));
        return ServerResponse.ok()
            .body(resourceRepository.findByTaskId(taskId), TaskResource.class);
    }
}
