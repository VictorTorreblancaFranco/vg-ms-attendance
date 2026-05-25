package com.vg.task.web.controller;

import com.vg.task.client.StudentClient;
import com.vg.task.client.TeacherClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/api/v1/user-selection")
@RequiredArgsConstructor
public class UserSelectionController {

    private final StudentClient studentClient;
    private final TeacherClient teacherClient;

    @GetMapping("/students")
    public Flux<StudentClient.StudentInfo> getAllStudents() {
        log.info("📚 GET /user-selection/students - Listando todos los estudiantes");
        return studentClient.getAllStudents();
    }

    @GetMapping("/teachers")
    public Flux<TeacherClient.TeacherInfo> getAllTeachers() {
        log.info("👨‍🏫 GET /user-selection/teachers - Listando todos los profesores");
        return teacherClient.getAllTeachers();
    }
}
