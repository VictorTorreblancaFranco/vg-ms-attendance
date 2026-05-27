package com.vg.task.service.port;

import reactor.core.publisher.Mono;

public interface AcademicServicePort {
    Mono<Boolean> validateClass(Integer classId);
    Mono<ClassInfo> getClassInfo(Integer classId);
    
    record ClassInfo(Integer id, Integer profesorId, Integer gradoId, Integer materiaId, 
                     Integer anoAcademicoId, Boolean activa) {}
}
