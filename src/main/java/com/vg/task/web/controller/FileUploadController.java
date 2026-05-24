package com.vg.task.web.controller;

import com.vg.task.service.impl.CloudinaryUploadService;
import com.vg.task.service.impl.RateLimitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class FileUploadController {
    private final CloudinaryUploadService cloudinaryUploadService;
    private final RateLimitService rateLimitService;

    @PostMapping("/task")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Map<String, String>> uploadTaskFile(@RequestPart("file") FilePart file) {
        return cloudinaryUploadService.upload(file, "task_files")
                .map(url -> Map.of("fileName", file.filename(), "fileUrl", url, "message", "File uploaded successfully"));
    }
}
