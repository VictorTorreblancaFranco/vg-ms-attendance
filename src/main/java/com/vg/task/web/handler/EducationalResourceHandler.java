package com.vg.task.web.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vg.task.domain.dto.EducationalResourceDTO;
import com.vg.task.service.impl.EducationalResourceServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class EducationalResourceHandler {

    private final EducationalResourceServiceImpl educationalResourceService;
    private final ObjectMapper objectMapper;

    public Mono<ServerResponse> findAll(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(educationalResourceService.findAll(), EducationalResourceDTO.class);
    }

    public Mono<ServerResponse> findBySubjectId(ServerRequest request) {
        Integer subjectId = Integer.parseInt(request.pathVariable("subjectId"));
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(educationalResourceService.findBySubjectId(subjectId), EducationalResourceDTO.class);
    }

    public Mono<ServerResponse> findByGradeId(ServerRequest request) {
        Integer gradeId = Integer.parseInt(request.pathVariable("gradeId"));
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(educationalResourceService.findByGradeId(gradeId), EducationalResourceDTO.class);
    }

    public Mono<ServerResponse> findPublic(ServerRequest request) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(educationalResourceService.findPublic(), EducationalResourceDTO.class);
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return educationalResourceService.findById(id)
                .flatMap(dto -> ServerResponse.ok().bodyValue(dto))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    // POST con JSON (sin archivo)
    public Mono<ServerResponse> saveJson(ServerRequest request) {
        return request.bodyToMono(EducationalResourceDTO.class)
                .flatMap(dto -> educationalResourceService.save(dto, null))
                .flatMap(dto -> ServerResponse.ok().bodyValue(dto));
    }

    // POST con archivo (multipart)
    public Mono<ServerResponse> saveWithFile(ServerRequest request) {
        return request.multipartData()
                .flatMap(parts -> {
                    String dtoJson = request.queryParam("dto").orElse("{}");
                    EducationalResourceDTO dto;
                    try {
                        dto = objectMapper.readValue(dtoJson, EducationalResourceDTO.class);
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Invalid DTO", e));
                    }
                    FilePart filePart = (FilePart) parts.toSingleValueMap().get("file");
                    return educationalResourceService.save(dto, filePart);
                })
                .flatMap(dto -> ServerResponse.ok().bodyValue(dto));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return request.bodyToMono(EducationalResourceDTO.class)
                .flatMap(dto -> educationalResourceService.update(id, dto))
                .flatMap(dto -> ServerResponse.ok().bodyValue(dto));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return educationalResourceService.delete(id)
                .then(ServerResponse.noContent().build());
    }
}
