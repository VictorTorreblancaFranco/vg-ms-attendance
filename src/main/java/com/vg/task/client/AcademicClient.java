package com.vg.task.client;

import com.vg.task.application.port.output.AcademicServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AcademicClient implements AcademicServicePort {
    
    @Override
    public Mono<Boolean> validateClass(Integer classId) {
        log.info("🔵 VALIDACIÓN DESHABILITADA - Clase: {} es válida automáticamente", classId);
        return Mono.just(true);
    }
    
    @Override
    public Mono<ClassInfo> getClassInfo(Integer classId) {
        return Mono.empty();
    }
}
