package com.vg.task.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.vg.task.constants.FileConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class CloudinaryUploadService {

    private final Cloudinary cloudinary;

    public CloudinaryUploadService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    public Mono<String> upload(FilePart filePart, String folder) {
        // Validar nombre de archivo
        String fileName = filePart.filename();
        if (!FileConstants.isAllowedFileType(fileName)) {
            String allowedTypes = String.join(", ", FileConstants.ALLOWED_FILE_TYPES);
            return Mono.error(new IllegalArgumentException(
                "Tipo de archivo no permitido. Permitidos: " + allowedTypes
            ));
        }
        
        return DataBufferUtils.join(filePart.content())
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    
                    // Validar tamaño
                    long fileSize = bytes.length;
                    if (fileSize > FileConstants.MAX_FILE_SIZE_BYTES) {
                        return Mono.error(new IllegalArgumentException(
                            "El archivo excede el tamaño máximo de " + FileConstants.MAX_FILE_SIZE_MB + " MB"
                        ));
                    }
                    
                    return Mono.fromCallable(() -> {
                        Path tempFile = Files.createTempFile(UUID.randomUUID().toString(), fileName);
                        Files.write(tempFile, bytes);
                        
                        Map params = ObjectUtils.asMap(
                                "folder", folder,
                                "resource_type", "auto",
                                "use_filename", true,
                                "unique_filename", true
                        );
                        Map result = cloudinary.uploader().upload(tempFile.toFile(), params);
                        Files.delete(tempFile);
                        return (String) result.get("secure_url");
                    });
                })
                .doOnError(error -> log.error("Error al subir archivo {}: {}", fileName, error.getMessage()));
    }
}
