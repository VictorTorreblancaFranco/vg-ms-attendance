package com.vg.task.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class CloudinaryUploadService {

    private final Cloudinary cloudinary;
    private final VirusScanService virusScanService;
    
    private static final long MAX_FILE_SIZE_MB = 10;
    private static final long MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024;
    
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "pdf", "doc", "docx", "jpg", "jpeg", "png", "txt", "xls", "xlsx", "mp4"
    );

    public CloudinaryUploadService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret,
            VirusScanService virusScanService) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
        this.virusScanService = virusScanService;
    }

    public Mono<String> upload(FilePart filePart, String folder) {
        String fileName = filePart.filename();
        
        log.info("📁 Recibiendo archivo: {}", fileName);
        
        String extension = getFileExtension(fileName);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            return Mono.error(new IllegalArgumentException(
                "❌ Tipo de archivo no permitido: " + extension + 
                ". Permitidos: " + String.join(", ", ALLOWED_EXTENSIONS)
            ));
        }
        
        return DataBufferUtils.join(filePart.content())
                .flatMap(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    
                    long fileSize = bytes.length;
                    if (fileSize > MAX_FILE_SIZE_BYTES) {
                        return Mono.error(new IllegalArgumentException(
                            "❌ El archivo excede el tamaño máximo de " + MAX_FILE_SIZE_MB + " MB"
                        ));
                    }
                    
                    return virusScanService.scan(bytes, fileName)
                        .flatMap(isSafe -> {
                            if (!isSafe) {
                                return Mono.error(new SecurityException(
                                    "❌ El archivo fue bloqueado por razones de seguridad"
                                ));
                            }
                            
                            log.info("✅ Archivo validado y escaneado: {}", fileName);
                            return uploadToCloudinary(bytes, fileName, folder);
                        });
                })
                .doOnError(error -> log.error("❌ Error con archivo {}: {}", fileName, error.getMessage()));
    }
    
    private Mono<String> uploadToCloudinary(byte[] bytes, String fileName, String folder) {
        return Mono.fromCallable(() -> {
            Path tempFile = Files.createTempFile(UUID.randomUUID().toString(), fileName);
            Files.write(tempFile, bytes);
            
            Map<String, Object> params = ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "auto",
                    "use_filename", true,
                    "unique_filename", true
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(tempFile.toFile(), params);
            Files.delete(tempFile);
            
            String url = (String) result.get("secure_url");
            log.info("✅ Archivo subido a Cloudinary: {}", url);
            return url;
        });
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
}
