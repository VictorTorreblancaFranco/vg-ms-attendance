package com.vg.task.web.controller;

import com.vg.task.domain.dto.CurriculumPlanDTO;
import com.vg.task.service.CurriculumPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/curriculum-plan")
@RequiredArgsConstructor
public class CurriculumPlanController {
    private final CurriculumPlanService curriculumPlanService;

    @GetMapping("/class/{classId}")
    public Flux<CurriculumPlanDTO> findByClassId(@PathVariable Integer classId) {
        return curriculumPlanService.findByClassId(classId);
    }

    @GetMapping("/{id}")
    public Mono<CurriculumPlanDTO> findById(@PathVariable Long id) {
        return curriculumPlanService.findById(id);
    }

    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CurriculumPlanDTO> save(@RequestBody CurriculumPlanDTO dto) {
        return curriculumPlanService.save(dto);
    }

    @PutMapping("/{id}")
    public Mono<CurriculumPlanDTO> update(@PathVariable Long id, @RequestBody CurriculumPlanDTO dto) {
        return curriculumPlanService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable Long id) {
        return curriculumPlanService.delete(id);
    }
}
