package com.vg.task.mapper;

import com.vg.task.domain.dto.SubmissionRequestDTO;
import com.vg.task.domain.dto.SubmissionResponseDTO;
import com.vg.task.domain.model.Submission;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Collections;

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
                .presented(false)
                .isLate(false)
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
                .presented(entity.getPresented())
                .presentedAt(entity.getPresentedAt())
                .observations(entity.getObservations())
                .isLate(entity.getIsLate())
                .justifiedAt(entity.getJustifiedAt())
                .justifiedBy(entity.getJustifiedBy())
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
        entity.setPresented(domain.getPresented());
        entity.setPresentedAt(domain.getPresentedAt());
        entity.setObservations(domain.getObservations());
        entity.setIsLate(domain.getIsLate());
        entity.setJustifiedAt(domain.getJustifiedAt());
        entity.setJustifiedBy(domain.getJustifiedBy());
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
                submission.getPresented(),
                submission.getPresentedAt(),
                submission.getObservations(),
                submission.getIsLate(),
                submission.getJustifiedAt(),
                submission.getJustifiedBy(),
                submission.getCreatedAt(),
                submission.getUpdatedAt()
        );
    }
}
