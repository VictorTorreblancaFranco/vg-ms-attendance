package com.vg.task.service;

import com.vg.task.domain.dto.EducationalResourceDTO;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EducationalResourceService {
    Flux<EducationalResourceDTO> findAll();
    Flux<EducationalResourceDTO> findBySubjectId(Integer subjectId);
    Flux<EducationalResourceDTO> findByGradeId(Integer gradeId);
    Flux<EducationalResourceDTO> findPublic();
    Mono<EducationalResourceDTO> findById(Long id);
    Mono<EducationalResourceDTO> save(EducationalResourceDTO dto, FilePart file);
    Mono<EducationalResourceDTO> update(Long id, EducationalResourceDTO dto);
    Mono<Void> delete(Long id);
}
