package com.vg.task.service;

import com.vg.task.client.AcademicClient;
import com.vg.task.client.StudentClient;
import com.vg.task.domain.dto.TeacherClassDTO;
import com.vg.task.domain.dto.TeacherSubjectsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherSubjectsService {

    private final AcademicClient academicClient;
    private final StudentClient studentClient;

    private static final Map<String, Set<Integer>> ESPECIALIDAD_MATERIAS = Map.of(
        "Matemáticas", Set.of(1, 4, 6),
        "Comunicacion", Set.of(2, 5, 7),
        "Ciencias", Set.of(3, 8, 9, 10, 11, 12)
    );

    @Cacheable(value = "teacher-subjects", key = "#teacherId", unless = "#result == null")
    public Mono<TeacherSubjectsDTO> getTeacherSubjects(Integer teacherId) {
        log.info("Obteniendo materias del profesor ID: {}", teacherId);
        
        return studentClient.getTeacherById(Long.valueOf(teacherId))
            .flatMap(teacher -> {
                String especialidad = teacher.specialty();
                Set<Integer> materiasPermitidas = ESPECIALIDAD_MATERIAS.getOrDefault(especialidad, Set.of());
                log.info("Profesor {} especialidad: {}, materias permitidas: {}", teacher.nombre(), especialidad, materiasPermitidas);
                
                return academicClient.getClasesByProfesor(teacherId)
                    .collectList()
                    .flatMap(clases -> {
                        if (clases.isEmpty()) {
                            return Mono.just(new TeacherSubjectsDTO(teacherId, teacher.nombre(), java.util.Collections.emptyList()));
                        }
                        
                        return Mono.zip(
                            academicClient.getAllMaterias().collectMap(m -> m.id(), m -> m.nombre()),
                            academicClient.getAllGrados().collectMap(g -> g.id(), g -> g.nombre())
                        ).map(tuple -> {
                            var materiasMap = tuple.getT1();
                            var gradosMap = tuple.getT2();
                            
                            var subjects = clases.stream()
                                .filter(clase -> materiasPermitidas.contains(clase.materiaId()))
                                .collect(Collectors.toMap(
                                    clase -> clase.materiaId() + "-" + clase.gradoId(),
                                    clase -> {
                                        String materiaNombre = materiasMap.getOrDefault(clase.materiaId(), "Materia " + clase.materiaId());
                                        String gradoNombre = gradosMap.getOrDefault(clase.gradoId(), "Grado " + clase.gradoId());
                                        return new TeacherSubjectsDTO.SubjectDTO(
                                            clase.materiaId(),
                                            materiaNombre,
                                            clase.gradoId(),
                                            gradoNombre,
                                            clase.id()
                                        );
                                    },
                                    (existing, replacement) -> existing
                                ))
                                .values()
                                .stream()
                                .sorted(Comparator.comparing(TeacherSubjectsDTO.SubjectDTO::subjectName))
                                .collect(Collectors.toList());
                            
                            log.info("Profesor {} tiene {} materias (filtradas por especialidad)", teacher.nombre(), subjects.size());
                            return new TeacherSubjectsDTO(teacherId, teacher.nombre(), subjects);
                        });
                    });
            })
            .switchIfEmpty(Mono.just(new TeacherSubjectsDTO(teacherId, "Profesor " + teacherId, java.util.Collections.emptyList())));
    }

    // NUEVO MÉTODO: Obtener clases completas del profesor (materia + grado + classId)
    @Cacheable(value = "teacher-classes", key = "#teacherId", unless = "#result == null")
    public Flux<TeacherClassDTO> getTeacherClasses(Integer teacherId) {
        log.info("Obteniendo clases completas del profesor ID: {}", teacherId);
        
        return academicClient.getClasesByProfesor(teacherId)
            .flatMap(clase -> 
                Mono.zip(
                    academicClient.getAllMaterias().collectMap(m -> m.id(), m -> m.nombre()).defaultIfEmpty(Map.of()),
                    academicClient.getAllGrados().collectMap(g -> g.id(), g -> g.nombre()).defaultIfEmpty(Map.of())
                ).map(tuple -> {
                    var materiasMap = tuple.getT1();
                    var gradosMap = tuple.getT2();
                    
                    return new TeacherClassDTO(
                        clase.id(),
                        clase.materiaId(),
                        materiasMap.getOrDefault(clase.materiaId(), "Materia " + clase.materiaId()),
                        clase.gradoId(),
                        gradosMap.getOrDefault(clase.gradoId(), "Grado " + clase.gradoId()),
                        clase.activa()
                    );
                })
            )
            .onErrorResume(e -> {
                log.error("Error obteniendo clases: {}", e.getMessage());
                return Flux.empty();
            });
    }
}
