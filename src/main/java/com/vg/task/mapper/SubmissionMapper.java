package com.vg.task.mapper;

import com.vg.task.domain.dto.SubmissionRequestDTO;
import com.vg.task.domain.dto.SubmissionResponseDTO;
import com.vg.task.domain.dto.SubmissionFileDTO;
import com.vg.task.domain.dto.RubricScoreDTO;
import com.vg.task.domain.model.Submission;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

@Component
public class SubmissionMapper {
    
    public Submission toDomain(SubmissionRequestDTO dto) {
        if (dto == null) return null;
        
        OffsetDateTime now = OffsetDateTime.now();
        
        return Submission.builder()
                .taskId(dto.taskId())
                .studentId(dto.studentId())
                .submissionDate(now)
                .status("submitted")
                .justificationReason(dto.justificationReason())
                .privateComment(dto.privateComment())
                .publicComment(dto.publicComment())
                .reattemptCount(1)
                .reattemptAllowed(false)
                .maxReattempts(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
    
    public Submission toDomain(com.vg.task.domain.model.Submission entity) {
        if (entity == null) return null;
        
        return Submission.builder()
                .id(entity.getId())
                .taskId(entity.getTaskId())
                .studentId(entity.getStudentId())
                .submissionDate(entity.getSubmissionDate())
                .status(entity.getStatus())
                .grade(entity.getGrade())
                .feedback(entity.getFeedback())
                .gradedBy(entity.getGradedBy())
                .gradedAt(entity.getGradedAt())
                .justificationReason(entity.getJustificationReason())
                .privateComment(entity.getPrivateComment())
                .publicComment(entity.getPublicComment())
                .reattemptCount(entity.getReattemptCount())
                .reattemptAllowed(entity.getReattemptAllowed())
                .maxReattempts(entity.getMaxReattempts())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    public com.vg.task.domain.model.Submission toEntity(Submission domain) {
        if (domain == null) return null;
        
        com.vg.task.domain.model.Submission entity = new com.vg.task.domain.model.Submission();
        entity.setId(domain.getId());
        entity.setTaskId(domain.getTaskId());
        entity.setStudentId(domain.getStudentId());
        entity.setSubmissionDate(domain.getSubmissionDate());
        entity.setStatus(domain.getStatus());
        entity.setGrade(domain.getGrade());
        entity.setFeedback(domain.getFeedback());
        entity.setGradedBy(domain.getGradedBy());
        entity.setGradedAt(domain.getGradedAt());
        entity.setJustificationReason(domain.getJustificationReason());
        entity.setPrivateComment(domain.getPrivateComment());
        entity.setPublicComment(domain.getPublicComment());
        entity.setReattemptCount(domain.getReattemptCount());
        entity.setReattemptAllowed(domain.getReattemptAllowed());
        entity.setMaxReattempts(domain.getMaxReattempts());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
    
    public SubmissionResponseDTO toResponse(Submission submission) {
        if (submission == null) return null;
        
        return new SubmissionResponseDTO(
                submission.getId(),
                submission.getTaskId(),
                submission.getStudentId(),
                submission.getSubmissionDate(),
                submission.getStatus(),
                submission.getGrade(),
                submission.getFeedback(),
                submission.getGradedBy(),
                submission.getGradedAt(),
                submission.getJustificationReason(),
                submission.getPrivateComment(),
                submission.getPublicComment(),
                submission.getReattemptCount(),
                submission.getReattemptAllowed(),
                submission.getMaxReattempts(),
                submission.getCreatedAt(),
                submission.getUpdatedAt(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );
    }
}
