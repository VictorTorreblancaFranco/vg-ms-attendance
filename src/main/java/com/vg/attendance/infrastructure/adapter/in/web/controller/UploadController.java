package com.vg.attendance.infrastructure.adapter.in.web.controller;

import com.vg.attendance.infrastructure.adapter.in.web.dto.UploadResponse;
import com.vg.attendance.infrastructure.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class UploadController {

    private final CloudinaryService cloudinaryService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<UploadResponse> uploadJustificacion(@RequestPart("file") FilePart filePart) {
        log.info("Uploading file: {}", filePart.filename());
        
        return DataBufferUtils.join(filePart.content())
            .flatMap(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                DataBufferUtils.release(dataBuffer);
                return cloudinaryService.uploadFile(bytes, filePart.filename());
            })
            .map(url -> UploadResponse.builder()
                .url(url)
                .message("Archivo subido correctamente")
                .success(true)
                .build())
            .onErrorResume(e -> {
                log.error("Upload error: {}", e.getMessage());
                return Mono.just(UploadResponse.builder()
                    .message("Error al subir archivo: " + e.getMessage())
                    .success(false)
                    .build());
            });
    }
}
