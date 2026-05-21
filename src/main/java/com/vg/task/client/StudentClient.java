package com.vg.task.client;

import com.vg.task.application.port.output.StudentServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentClient implements StudentServicePort {
    
    @Qualifier("studentWebClient")
    private final WebClient studentWebClient;
    
    @Override
    public Mono<Boolean> validateStudent(Integer studentId) {
        return studentWebClient.get()
                .uri("/api/students/{id}", studentId)
                .retrieve()
                .bodyToMono(Void.class)
                .thenReturn(true)
                .onErrorResume(e -> {
                    log.error("Error validating student {}: {}", studentId, e.getMessage());
                    return Mono.just(false);
                });
    }
    
    @Override
    public Mono<StudentInfo> getStudentInfo(Integer studentId) {
        return studentWebClient.get()
                .uri("/api/students/{id}", studentId)
                .retrieve()
                .bodyToMono(StudentInfo.class)
                .onErrorResume(e -> {
                    log.error("Error getting student info {}: {}", studentId, e.getMessage());
                    return Mono.empty();
                });
    }
}
