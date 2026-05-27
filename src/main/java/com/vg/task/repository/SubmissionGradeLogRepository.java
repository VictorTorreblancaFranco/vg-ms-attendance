package com.vg.task.repository;

import com.vg.task.domain.model.SubmissionGradeLog;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Repository
public interface SubmissionGradeLogRepository extends ReactiveCrudRepository<SubmissionGradeLog, Long> {
    
    default Mono<SubmissionGradeLog> saveLog(Long submissionId, Double oldGrade, Double newGrade, Integer changedBy, String justification) {
        SubmissionGradeLog log = SubmissionGradeLog.builder()
                .submissionId(submissionId)
                .oldGrade(oldGrade)
                .newGrade(newGrade)
                .changedBy(changedBy)
                .justification(justification)
                .changedAt(OffsetDateTime.now())
                .build();
        return save(log);
    }
}
