package com.vg.task.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudentClient {

    @Qualifier("studentWebClient")
    private final WebClient studentWebClient;

    public Mono<Boolean> validateStudent(Integer studentId) {
        return studentWebClient.get()
                .uri("/api/students/{id}", studentId)
                .retrieve()
                .bodyToMono(StudentResponse.class)
                .map(s -> true)
                .onErrorReturn(false);
    }

    public Mono<StudentInfo> getStudentInfo(Integer studentId) {
        return studentWebClient.get()
                .uri("/api/students/{id}", studentId)
                .retrieve()
                .bodyToMono(StudentResponse.class)
                .flatMap(student -> getPersonInfo(student.personId())
                        .map(person -> new StudentInfo(student.id(), student.studentCode(),
                                person != null ? person.getFullName() : student.studentCode(), true, student.gradeId())))
                .onErrorResume(e -> Mono.empty());
    }

    public Flux<StudentInfo> getAllStudents() {
        return studentWebClient.get()
                .uri("/api/students")
                .retrieve()
                .bodyToFlux(StudentResponse.class)
                .flatMap(student -> getPersonInfo(student.personId())
                        .map(person -> new StudentInfo(student.id(), student.studentCode(),
                                person != null ? person.getFullName() : student.studentCode(), true, student.gradeId())))
                .onErrorResume(e -> {
                    log.error("❌ Error obteniendo estudiantes: {}", e.getMessage());
                    return Flux.empty();
                });
    }

    public Flux<StudentInfo> getStudentsByGradeId(Integer gradeId) {
        return studentWebClient.get()
                .uri("/api/students?gradeId={gradeId}", gradeId)
                .retrieve()
                .bodyToFlux(StudentResponse.class)
                .flatMap(student -> getPersonInfo(student.personId())
                        .map(person -> new StudentInfo(student.id(), student.studentCode(),
                                person != null ? person.getFullName() : student.studentCode(), true, student.gradeId())))
                .onErrorResume(e -> Flux.empty());
    }

    public Mono<Integer> getStudentGradeId(Integer studentId) {
        return getStudentInfo(studentId).map(StudentInfo::gradeId);
    }

    private Mono<PersonInfo> getPersonInfo(Long personId) {
        return studentWebClient.get()
                .uri("/api/people/{id}", personId)
                .retrieve()
                .bodyToMono(PersonInfo.class)
                .onErrorResume(e -> Mono.empty());
    }

    public record StudentResponse(Integer id, Long personId, String studentCode, Boolean isActive, Integer gradeId) {}
    public record PersonInfo(Long id, String firstName, String lastName, String secondLastName) {
        public String getFullName() {
            String full = firstName + " " + lastName;
            if (secondLastName != null && !secondLastName.isBlank()) {
                full += " " + secondLastName;
            }
            return full;
        }
    }
    public record StudentInfo(Integer id, String studentCode, String nombre, Boolean active, Integer gradeId) {}
}
