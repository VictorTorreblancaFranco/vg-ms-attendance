package com.vg.task.service.impl;

import com.vg.task.domain.dto.EducationalResourceDTO;
import com.vg.task.domain.model.EducationalResource;
import com.vg.task.domain.model.exceptions.NotFoundException;
import com.vg.task.repository.EducationalResourceRepository;
import com.vg.task.service.EducationalResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class EducationalResourceServiceImpl implements EducationalResourceService {

    private final EducationalResourceRepository educationalResourceRepository;
    private final CloudinaryUploadService cloudinaryUploadService;

    @Override
    public Flux<EducationalResourceDTO> findAll() {
        return educationalResourceRepository.findAll()
                .map(this::toDTO);
    }

    @Override
    public Flux<EducationalResourceDTO> findBySubjectId(Integer subjectId) {
        return educationalResourceRepository.findBySubjectId(subjectId)
                .map(this::toDTO);
    }

    @Override
    public Flux<EducationalResourceDTO> findByGradeId(Integer gradeId) {
        return educationalResourceRepository.findByGradeId(gradeId)
                .map(this::toDTO);
    }

    @Override
    public Flux<EducationalResourceDTO> findPublic() {
        return educationalResourceRepository.findByIsPublicTrue()
                .map(this::toDTO);
    }

    @Override
    public Mono<EducationalResourceDTO> findById(Long id) {
        return educationalResourceRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Educational resource not found: " + id)))
                .map(this::toDTO);
    }

    @Override
    public Mono<EducationalResourceDTO> save(EducationalResourceDTO dto, FilePart file) {
        log.info("📁 Guardando recurso educativo: {}", dto.title());
        
        Mono<String> fileUrlMono = (file != null) 
                ? cloudinaryUploadService.upload(file, "educational_resources")
                : Mono.just(dto.url() != null ? dto.url() : "");

        return fileUrlMono.flatMap(url -> {
            EducationalResource resource = EducationalResource.builder()
                    .title(dto.title())
                    .description(dto.description())
                    .type(dto.type())
                    .url(url)
                    .filePath(dto.filePath())
                    .subjectId(dto.subjectId())
                    .gradeId(dto.gradeId())
                    .createdBy(dto.createdBy())
                    .isPublic(dto.isPublic() != null ? dto.isPublic() : true)
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();
            return educationalResourceRepository.save(resource);
        }).map(this::toDTO);
    }

    @Override
    public Mono<EducationalResourceDTO> update(Long id, EducationalResourceDTO dto) {
        return educationalResourceRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Educational resource not found: " + id)))
                .flatMap(existing -> {
                    if (dto.title() != null) existing.setTitle(dto.title());
                    if (dto.description() != null) existing.setDescription(dto.description());
                    if (dto.type() != null) existing.setType(dto.type());
                    if (dto.url() != null) existing.setUrl(dto.url());
                    if (dto.subjectId() != null) existing.setSubjectId(dto.subjectId());
                    if (dto.gradeId() != null) existing.setGradeId(dto.gradeId());
                    if (dto.isPublic() != null) existing.setIsPublic(dto.isPublic());
                    existing.setUpdatedAt(OffsetDateTime.now());
                    return educationalResourceRepository.save(existing);
                })
                .map(this::toDTO);
    }

    @Override
    public Mono<Void> delete(Long id) {
        return educationalResourceRepository.deleteById(id);
    }

    private EducationalResourceDTO toDTO(EducationalResource resource) {
        return new EducationalResourceDTO(
                resource.getId(),
                resource.getTitle(),
                resource.getDescription(),
                resource.getType(),
                resource.getUrl(),
                resource.getFilePath(),
                resource.getSubjectId(),
                resource.getGradeId(),
                resource.getCreatedBy(),
                resource.getIsPublic()
        );
    }
}
