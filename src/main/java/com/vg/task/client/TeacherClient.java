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
public class TeacherClient {

    @Qualifier("studentWebClient")
    private final WebClient studentWebClient;

    public Flux<TeacherInfo> getAllTeachers() {
        return studentWebClient.get()
                .uri("/api/teachers")
                .retrieve()
                .bodyToFlux(TeacherResponse.class)
                .flatMap(teacher -> getPersonInfo(teacher.personId())
                        .map(person -> new TeacherInfo(teacher.id(), teacher.teacherCode(),
                                person != null ? person.getFullName() : teacher.teacherCode(),
                                teacher.specialty(), teacher.isActive())))
                .onErrorResume(e -> Flux.empty());
    }

    public Mono<TeacherInfo> getTeacherById(Long id) {
        return studentWebClient.get()
                .uri("/api/teachers/{id}", id)
                .retrieve()
                .bodyToMono(TeacherResponse.class)
                .flatMap(teacher -> getPersonInfo(teacher.personId())
                        .map(person -> new TeacherInfo(teacher.id(), teacher.teacherCode(),
                                person != null ? person.getFullName() : teacher.teacherCode(),
                                teacher.specialty(), teacher.isActive())))
                .onErrorResume(e -> Mono.empty());
    }

    private Mono<PersonInfo> getPersonInfo(Long personId) {
        return studentWebClient.get()
                .uri("/api/people/{id}", personId)
                .retrieve()
                .bodyToMono(PersonInfo.class)
                .onErrorResume(e -> Mono.empty());
    }

    public record TeacherResponse(Long id, Long personId, String teacherCode, String specialty, Boolean isActive) {}
    public record PersonInfo(Long id, String firstName, String lastName, String secondLastName) {
        public String getFullName() {
            String full = firstName + " " + lastName;
            if (secondLastName != null && !secondLastName.isBlank()) {
                full += " " + secondLastName;
            }
            return full;
        }
    }
    public record TeacherInfo(Long id, String teacherCode, String nombre, String specialty, Boolean active) {}
}
