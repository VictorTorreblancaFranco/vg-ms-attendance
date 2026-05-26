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

    public Mono<TeacherInfo> getTeacherById(Long teacherId) {
        return studentWebClient.get()
                .uri("/api/teachers/{id}", teacherId)
                .retrieve()
                .bodyToMono(TeacherResponse.class)
                .flatMap(teacher -> getPersonInfo(teacher.personId())
                        .map(person -> new TeacherInfo(teacher.id(), teacher.teacherCode(),
                                person != null ? person.getFullName() : teacher.teacherCode(),
                                teacher.specialty(), teacher.isActive())))
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
                .onErrorResume(e -> Flux.empty());
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
    public record TeacherResponse(Long id, Long personId, String teacherCode, String specialty, Boolean isActive) {}
    public record TeacherInfo(Long id, String teacherCode, String nombre, String specialty, Boolean active) {}
}
