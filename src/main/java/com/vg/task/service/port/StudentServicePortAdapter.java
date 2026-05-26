package com.vg.task.service.port;

import com.vg.task.client.StudentClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class StudentServicePortAdapter implements StudentServicePort {

    private final StudentClient studentClient;

    @Override
    public Mono<Boolean> validateStudent(Integer studentId) {
        return studentClient.validateStudent(studentId);
    }

    @Override
    public Mono<StudentInfo> getStudentInfo(Integer studentId) {
        return studentClient.getStudentInfo(studentId)
                .map(s -> new StudentInfo(s.id(), s.studentCode(), s.nombre(), s.active(), s.gradeId()));
    }
}
