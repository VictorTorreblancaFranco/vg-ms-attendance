package com.vg.task.repository;

import com.vg.task.application.port.output.SubmissionRepositoryPort;
import com.vg.task.domain.model.Submission;
import com.vg.task.mapper.SubmissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SubmissionRepositoryAdapter implements SubmissionRepositoryPort {
    
    private final SubmissionRepository submissionRepository;
    private final SubmissionMapper mapper;
    
    @Override
    public Flux<Submission> findAll() {
        return submissionRepository.findAll()
                .map(mapper::toDomain);
    }
    
    @Override
    public Mono<Submission> findById(Long id) {
        return submissionRepository.findById(id)
                .map(mapper::toDomain);
    }
    
    @Override
    public Flux<Submission> findByTaskId(Long taskId) {
        return submissionRepository.findByTaskId(taskId)
                .map(mapper::toDomain);
    }
    
    @Override
    public Flux<Submission> findByTaskId(Long taskId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return submissionRepository.findByTaskId(taskId, pageable)
                .map(mapper::toDomain);
    }
    
    @Override
    public Flux<Submission> findByStudentId(Integer studentId) {
        return submissionRepository.findByStudentId(studentId)
                .map(mapper::toDomain);
    }
    
    @Override
    public Flux<Submission> findByStudentId(Integer studentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return submissionRepository.findByStudentId(studentId, pageable)
                .map(mapper::toDomain);
    }
    
    @Override
    public Mono<Submission> findByTaskIdAndStudentId(Long taskId, Integer studentId) {
        return submissionRepository.findByTaskIdAndStudentId(taskId, studentId)
                .map(mapper::toDomain);
    }
    
    @Override
    public Mono<Submission> save(Submission submission) {
        com.vg.task.domain.model.Submission entity = mapper.toEntity(submission);
        return submissionRepository.save(entity)
                .map(mapper::toDomain);
    }
    
    @Override
    public Mono<Void> deleteById(Long id) {
        return submissionRepository.deleteById(id);
    }
    
    @Override
    public Mono<Boolean> existsByTaskIdAndStudentId(Long taskId, Integer studentId) {
        return submissionRepository.existsByTaskIdAndStudentId(taskId, studentId);
    }
    
    @Override
    public Mono<Long> countByTaskId(Long taskId) {
        return submissionRepository.countByTaskId(taskId);
    }
    
    @Override
    public Mono<Long> countByStudentId(Integer studentId) {
        return submissionRepository.countByStudentId(studentId);
    }
}
