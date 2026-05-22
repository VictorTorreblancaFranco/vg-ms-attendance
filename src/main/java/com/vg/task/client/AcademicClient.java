package com.vg.task.client;

import com.vg.task.application.port.output.AcademicServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AcademicClient implements AcademicServicePort {
    
    @Qualifier("academicWebClient")
    private final WebClient academicWebClient;
    
    @Override
    public Mono<Boolean> validateClass(Integer classId) {
        log.info("🔍 Validando clase {} contra academic-service", classId);
        return academicWebClient.get()
                .uri("/api/clases/{id}", classId)
                .retrieve()
                .bodyToMono(ClassInfo.class)
                .map(classInfo -> {
                    log.info("✅ Clase {} encontrada: {}", classId, classInfo);
                    return classInfo != null && Boolean.TRUE.equals(classInfo.activa());
                })
                .onErrorResume(e -> {
                    log.error("❌ Error validando clase {}: {}", classId, e.getMessage());
                    return Mono.just(false);
                });
    }
    
    @Override
    public Mono<ClassInfo> getClassInfo(Integer classId) {
        log.info("🔍 Obteniendo info de clase {}", classId);
        return academicWebClient.get()
                .uri("/api/clases/{id}", classId)
                .retrieve()
                .bodyToMono(ClassInfo.class)
                .onErrorResume(e -> {
                    log.error("❌ Error obteniendo clase {}: {}", classId, e.getMessage());
                    return Mono.empty();
                });
    }
}
