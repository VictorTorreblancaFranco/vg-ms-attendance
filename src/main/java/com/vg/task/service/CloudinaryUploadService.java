package com.vg.task.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

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
        return DataBufferUtils.join(filePart.content())
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    
                    return Mono.fromCallable(() -> {
                        Path tempFile = Files.createTempFile(UUID.randomUUID().toString(), filePart.filename());
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
                });
    }
}
