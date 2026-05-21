package com.vg.task.application.port.output;

import reactor.core.publisher.Mono;

public interface StudentServicePort {
    Mono<Boolean> validateStudent(Integer studentId);
    Mono<StudentInfo> getStudentInfo(Integer studentId);
    
    record StudentInfo(Integer id, String studentCode, String nombre, Boolean active) {}
}
