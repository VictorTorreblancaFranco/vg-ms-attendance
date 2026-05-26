package com.vg.task.web.controller;

import com.vg.task.domain.dto.TeacherClassDTO;
import com.vg.task.domain.dto.TeacherSubjectsDTO;
import com.vg.task.service.TeacherSubjectsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/teacher")
@RequiredArgsConstructor
public class TeacherSubjectsController {

    private final TeacherSubjectsService teacherSubjectsService;

    @GetMapping("/{teacherId}/subjects")
    public Mono<TeacherSubjectsDTO> getTeacherSubjects(@PathVariable Integer teacherId) {
        log.info("👨‍🏫 GET /teacher/{}/subjects", teacherId);
        return teacherSubjectsService.getTeacherSubjects(teacherId);
    }

    // NUEVO ENDPOINT: Obtener clases completas del profesor
    @GetMapping("/{teacherId}/classes")
    public Flux<TeacherClassDTO> getTeacherClasses(@PathVariable Integer teacherId) {
        log.info("📚 GET /teacher/{}/classes", teacherId);
        return teacherSubjectsService.getTeacherClasses(teacherId);
    }
}
