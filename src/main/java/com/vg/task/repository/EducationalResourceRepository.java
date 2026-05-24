package com.vg.task.repository;

import com.vg.task.domain.model.EducationalResource;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface EducationalResourceRepository extends ReactiveCrudRepository<EducationalResource, Long> {
    Flux<EducationalResource> findBySubjectId(Integer subjectId);
    Flux<EducationalResource> findByGradeId(Integer gradeId);
    Flux<EducationalResource> findByIsPublicTrue();
}
