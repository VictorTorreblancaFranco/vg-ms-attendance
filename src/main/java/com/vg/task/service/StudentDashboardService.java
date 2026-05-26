package com.vg.task.service;

import com.vg.task.client.AcademicClient;
import com.vg.task.client.StudentClient;
import com.vg.task.domain.dto.StudentDashboardDTO;
import com.vg.task.domain.model.Submission;
import com.vg.task.repository.SubmissionRepository;
import com.vg.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentDashboardService {

    private final StudentClient studentClient;
    private final AcademicClient academicClient;
    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;

    public Mono<StudentDashboardDTO> getDashboard(Integer studentId) {
        log.info("Dashboard para estudiante: {}", studentId);
        
        return studentClient.getStudentInfo(studentId)
            .switchIfEmpty(Mono.error(new RuntimeException("Estudiante no encontrado: " + studentId)))
            .flatMap(student -> {
                Integer gradoId = student.gradeId();
                log.info("Estudiante {} tiene gradoId: {}", studentId, gradoId);
                
                return academicClient.getMateriasConClaseByGrado(gradoId)
                    .collectList()
                    .flatMap(materias -> {
                        if (materias.isEmpty()) {
                            log.warn("No hay materias para grado {}", gradoId);
                            return Mono.just(new StudentDashboardDTO(
                                studentId, student.nombre(), Collections.emptyList()
                            ));
                        }
                        
                        // Eliminar duplicados por materiaId
                        var materiasUnicas = materias.stream()
                            .collect(Collectors.toMap(
                                AcademicClient.MateriaConClaseDTO::materiaId,
                                m -> m,
                                (existing, replacement) -> existing
                            ))
                            .values()
                            .stream()
                            .collect(Collectors.toList());
                        
                        var classIds = materiasUnicas.stream()
                            .map(AcademicClient.MateriaConClaseDTO::claseId)
                            .collect(Collectors.toList());
                        
                        log.info("Materias unicas para grado {}: {}", gradoId, materiasUnicas.size());
                        
                        return taskRepository.findByClassIdInAndStatusAndIsDeletedFalse(classIds, "published")
                            .collectList()
                            .defaultIfEmpty(Collections.emptyList())
                            .zipWith(submissionRepository.findByStudentId(studentId)
                                .collectMap(Submission::getTaskId, s -> s)
                                .defaultIfEmpty(Collections.emptyMap()))
                            .map(tuple -> {
                                var tasks = tuple.getT1();
                                var submissions = tuple.getT2();
                                
                                // Eliminar tareas duplicadas por taskId
                                var tasksUnicas = tasks.stream()
                                    .collect(Collectors.toMap(
                                        t -> t.getId(),
                                        t -> t,
                                        (existing, replacement) -> existing
                                    ))
                                    .values()
                                    .stream()
                                    .collect(Collectors.toList());
                                
                                var materiasDTO = materiasUnicas.stream().map(materia -> {
                                    var tareasDTO = tasksUnicas.stream()
                                        .filter(t -> t.getClassId().equals(materia.claseId()))
                                        .map(tarea -> {
                                            var sub = submissions.get(tarea.getId());
                                            boolean entregada = sub != null && Boolean.TRUE.equals(sub.getPresented());
                                            String estado = "pendiente";
                                            if (sub != null) {
                                                if (sub.getGrade() != null) {
                                                    estado = "calificado";
                                                } else if (Boolean.TRUE.equals(sub.getPresented())) {
                                                    estado = "entregado";
                                                }
                                            }
                                            return new StudentDashboardDTO.TareaDTO(
                                                tarea.getId(),
                                                tarea.getTitle(),
                                                tarea.getDescription(),
                                                tarea.getDueDate(),
                                                entregada,
                                                sub != null ? sub.getGrade() : null,
                                                estado
                                            );
                                        }).collect(Collectors.toList());
                                    
                                    return new StudentDashboardDTO.MateriaDTO(
                                        materia.materiaId(),
                                        materia.materiaNombre(),
                                        materia.gradoId(),
                                        materia.gradoNombre(),
                                        tareasDTO
                                    );
                                }).collect(Collectors.toList());
                                
                                return new StudentDashboardDTO(studentId, student.nombre(), materiasDTO);
                            });
                    });
            });
    }
}
