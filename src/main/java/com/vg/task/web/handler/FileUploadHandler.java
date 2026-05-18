package com.vg.task.web.handler;

import com.vg.task.service.CloudinaryUploadService;
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

    public Mono<ServerResponse> uploadTaskFile(ServerRequest request) {
        return request.multipartData()
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
            });
    }
}
