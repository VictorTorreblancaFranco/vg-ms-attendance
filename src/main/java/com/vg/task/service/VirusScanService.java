package com.vg.task.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Set;

@Slf4j
@Service
public class VirusScanService {

    // Extensiones peligrosas bloqueadas
    private static final Set<String> DANGEROUS_EXTENSIONS = Set.of(
        "exe", "bat", "cmd", "sh", "msi", "vbs", "ps1", "jar", "class",
        "dll", "so", "dylib", "scr", "com", "reg", "bin", "app"
    );

    // Palabras clave sospechosas en el contenido (simulado)
    private static final Set<String> SUSPICIOUS_PATTERNS = Set.of(
        "<script", "javascript:", "eval(", "document.write", "ActiveX",
        "CreateObject", "WScript", "Shell", "Run(", "Exec(", "Invoke-"
    );

    public Mono<Boolean> scan(byte[] content, String fileName) {
        return Mono.fromCallable(() -> {
            String extension = getFileExtension(fileName).toLowerCase();
            
            // 1. Validar extensión peligrosa
            if (DANGEROUS_EXTENSIONS.contains(extension)) {
                log.warn("🔴 Archivo bloqueado por extensión peligrosa: {}", fileName);
                return false;
            }
            
            // 2. Buscar contenido sospechoso (para texto plano)
            String contentStr = new String(content);
            for (String pattern : SUSPICIOUS_PATTERNS) {
                if (contentStr.toLowerCase().contains(pattern.toLowerCase())) {
                    log.warn("🔴 Archivo bloqueado por contenido sospechoso: {}", fileName);
                    return false;
                }
            }
            
            // 3. Detectar macros en archivos de Office (simplificado)
            if (isOfficeFile(extension)) {
                if (contentStr.contains("Macro") || contentStr.contains("VBA")) {
                    log.warn("🔴 Archivo bloqueado por posible macro maliciosa: {}", fileName);
                    return false;
                }
            }
            
            log.info("✅ Archivo escaneado sin amenazas: {}", fileName);
            return true;
        });
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1);
    }
    
    private boolean isOfficeFile(String extension) {
        return Set.of("doc", "docx", "xls", "xlsx", "ppt", "pptx").contains(extension);
    }
}
