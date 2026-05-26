package com.vg.task.service;

import com.vg.task.client.AcademicClient;
import com.vg.task.domain.dto.TeacherSubjectsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherSubjectsService {

    private final AcademicClient academicClient;

    public Mono<TeacherSubjectsDTO> getTeacherSubjects(Integer teacherId) {
        log.info("👨‍🏫 Obteniendo materias del profesor: {}", teacherId);
        
        return academicClient.getClasesByProfesor(teacherId)
            .collectList()
            .map(clases -> {
                var subjects = clases.stream().map(clase -> 
                    new TeacherSubjectsDTO.SubjectDTO(
                        clase.materiaId(),
                        "Materia " + clase.materiaId(),
                        clase.gradoId(),
                        "Grado " + clase.gradoId(),
                        clase.id()
                    )
                ).collect(Collectors.toList());
                
                return new TeacherSubjectsDTO(
                    teacherId,
                    "Profesor " + teacherId,
                    subjects
                );
            })
            .defaultIfEmpty(new TeacherSubjectsDTO(teacherId, "Profesor " + teacherId, java.util.Collections.emptyList()));
    }
}
