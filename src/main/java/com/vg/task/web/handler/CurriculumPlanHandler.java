package com.vg.task.web.handler;

import com.vg.task.domain.dto.CurriculumPlanDTO;
import com.vg.task.service.impl.CurriculumPlanServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CurriculumPlanHandler {

    private final CurriculumPlanServiceImpl curriculumPlanService;

    public Mono<ServerResponse> findByClassId(ServerRequest request) {
        Integer classId = Integer.parseInt(request.pathVariable("classId"));
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(curriculumPlanService.findByClassId(classId), CurriculumPlanDTO.class);
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return curriculumPlanService.findById(id)
                .flatMap(dto -> ServerResponse.ok().bodyValue(dto))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> save(ServerRequest request) {
        return request.bodyToMono(CurriculumPlanDTO.class)
                .flatMap(curriculumPlanService::save)
                .flatMap(dto -> ServerResponse.ok().bodyValue(dto));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return request.bodyToMono(CurriculumPlanDTO.class)
                .flatMap(dto -> curriculumPlanService.update(id, dto))
                .flatMap(dto -> ServerResponse.ok().bodyValue(dto));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return curriculumPlanService.delete(id)
                .then(ServerResponse.noContent().build());
    }
}
