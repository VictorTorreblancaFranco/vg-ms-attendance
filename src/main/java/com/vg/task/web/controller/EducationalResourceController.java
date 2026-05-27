package com.vg.task.web.controller;

import com.vg.task.domain.dto.EducationalResourceDTO;
import com.vg.task.service.EducationalResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/educational-resources")
@RequiredArgsConstructor
public class EducationalResourceController {
    private final EducationalResourceService educationalResourceService;

    @GetMapping
    public Flux<EducationalResourceDTO> findAll() {
        return educationalResourceService.findAll();
    }

    @GetMapping("/public")
    public Flux<EducationalResourceDTO> findPublic() {
        return educationalResourceService.findPublic();
    }

    @GetMapping("/subject/{subjectId}")
    public Flux<EducationalResourceDTO> findBySubjectId(@PathVariable Integer subjectId) {
        return educationalResourceService.findBySubjectId(subjectId);
    }

    @GetMapping("/grade/{gradeId}")
    public Flux<EducationalResourceDTO> findByGradeId(@PathVariable Integer gradeId) {
        return educationalResourceService.findByGradeId(gradeId);
    }

    @GetMapping("/{id}")
    public Mono<EducationalResourceDTO> findById(@PathVariable Long id) {
        return educationalResourceService.findById(id);
    }

    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<EducationalResourceDTO> save(@RequestBody EducationalResourceDTO dto) {
        return educationalResourceService.save(dto, null);
    }
}
