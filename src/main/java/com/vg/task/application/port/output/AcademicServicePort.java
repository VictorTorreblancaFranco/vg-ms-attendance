package com.vg.task.application.port.output;

import reactor.core.publisher.Mono;

public interface AcademicServicePort {
    Mono<Boolean> validateClass(Integer classId);
    Mono<ClassInfo> getClassInfo(Integer classId);
    
    record ClassInfo(Integer id, Integer profesorId, String materiaNombre, Boolean activa) {}
}
