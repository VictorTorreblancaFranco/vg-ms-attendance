package com.vg.task.web.handler;

import com.vg.task.service.CloudinaryUploadService;
import com.vg.task.service.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class FileUploadHandler {

    private final CloudinaryUploadService cloudinaryUploadService;
    private final RateLimitService rateLimitService;

    private Mono<ServerResponse> checkRateLimit(ServerRequest request) {
        String clientIp = request.remoteAddress()
                .map(addr -> addr.getAddress().getHostAddress())
                .orElse("unknown");
        String path = request.path();

        return rateLimitService.allowRequest(clientIp, path)
                .flatMap(allowed -> {
                    if (!allowed) {
                        return ServerResponse.status(429)
                                .bodyValue(Map.of(
                                        "error", "Too Many Requests",
                                        "message", "Has excedido el límite de peticiones para este endpoint",
                                        "status", 429
                                ));
                    }
                    return Mono.empty();
                });
    }

    private Mono<ServerResponse> withRateLimit(ServerRequest request, Mono<ServerResponse> response) {
        return checkRateLimit(request).switchIfEmpty(response);
    }

    public Mono<ServerResponse> uploadTaskFile(ServerRequest request) {
        return withRateLimit(request, request.multipartData()
                .flatMap(parts -> {
                    FilePart filePart = (FilePart) parts.toSingleValueMap().get("file");
                    if (filePart == null) {
                        return ServerResponse.badRequest()
                                .bodyValue(Map.of("error", "File is required"));
                    }

                    return cloudinaryUploadService.upload(filePart, "task_files")
                            .flatMap(url -> ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(Map.of(
                                            "fileName", filePart.filename(),
                                            "fileUrl", url,
                                            "message", "File uploaded successfully"
                                    )))
                            .onErrorResume(e -> ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .bodyValue(Map.of("error", e.getMessage())));
                }));
    }
}
