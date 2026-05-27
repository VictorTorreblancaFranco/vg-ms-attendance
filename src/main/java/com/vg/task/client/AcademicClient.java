package com.vg.task.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AcademicClient {

    @Qualifier("academicWebClient")
    private final WebClient academicWebClient;

    @Cacheable(value = "classes", key = "#classId")
    public Mono<Boolean> validateClass(Integer classId) {
        log.info("Validando clase {}", classId);
        return academicWebClient.get()
                .uri("/api/clases/{id}", classId)
                .retrieve()
                .bodyToMono(ClassInfo.class)
                .map(classInfo -> classInfo != null && Boolean.TRUE.equals(classInfo.activa()))
                .onErrorReturn(false);
    }

    @Cacheable(value = "classes", key = "#classId")
    public Mono<ClassInfo> getClassInfo(Integer classId) {
        return academicWebClient.get()
                .uri("/api/clases/{id}", classId)
                .retrieve()
                .bodyToMono(ClassInfo.class)
                .onErrorResume(e -> Mono.empty());
    }

    public Flux<ClasePorProfesorDTO> getClasesByProfesor(Integer profesorId) {
        log.info("Obteniendo clases del profesor: {}", profesorId);
        return academicWebClient.get()
                .uri("/api/clases/profesor/{profesorId}", profesorId)
                .retrieve()
                .bodyToFlux(ClasePorProfesorDTO.class)
                .onErrorResume(e -> {
                    log.error("Error: {}", e.getMessage());
                    return Flux.empty();
                });
    }

    public Flux<MateriaConClaseDTO> getMateriasConClaseByGrado(Integer gradoId) {
        log.info("Obteniendo materias para grado: {}", gradoId);
        return academicWebClient.get()
                .uri("/api/clases", gradoId)
                .retrieve()
                .bodyToFlux(ClassInfo.class)
                .filter(clase -> clase.gradoId().equals(gradoId))
                .collectList()
                .map(list -> list.stream()
                    .collect(Collectors.toMap(
                        ClassInfo::materiaId,
                        clase -> new MateriaConClaseDTO(
                            clase.materiaId(),
                            "Materia " + clase.materiaId(),
                            clase.id(),
                            clase.gradoId(),
                            "Grado " + clase.gradoId(),
                            clase.activa()
                        ),
                        (existing, replacement) -> existing
                    ))
                    .values()
                    .stream()
                    .collect(Collectors.toList())
                )
                .flatMapMany(Flux::fromIterable)
                .onErrorResume(e -> {
                    log.error("Error: {}", e.getMessage());
                    return Flux.empty();
                });
    }

    public Flux<MateriaInfo> getAllMaterias() {
        return academicWebClient.get()
                .uri("/api/materias")
                .retrieve()
                .bodyToFlux(MateriaInfo.class)
                .onErrorResume(e -> {
                    log.error("Error obteniendo materias: {}", e.getMessage());
                    return Flux.empty();
                });
    }

    public Flux<GradoInfo> getAllGrados() {
        return academicWebClient.get()
                .uri("/api/grados")
                .retrieve()
                .bodyToFlux(GradoInfo.class)
                .onErrorResume(e -> {
                    log.error("Error obteniendo grados: {}", e.getMessage());
                    return Flux.empty();
                });
    }

    public record ClassInfo(Integer id, Integer profesorId, Integer gradoId, Integer materiaId, 
                            Integer aulaId, Integer anoAcademicoId, String modalidad, Boolean activa) {}
    
    public record ClasePorProfesorDTO(Integer id, Integer profesorId, Integer gradoId, Integer materiaId, 
                                      Integer aulaId, Integer anoAcademicoId, String modalidad, Boolean activa) {}
    
    public record MateriaConClaseDTO(Integer materiaId, String materiaNombre, Integer claseId, 
                                     Integer gradoId, String gradoNombre, Boolean claseActiva) {}
    
    public record MateriaInfo(Integer id, String nombre) {}
    
    public record GradoInfo(Integer id, String nombre) {}
}
