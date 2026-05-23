package com.vg.task.mapper;

import com.vg.task.domain.dto.TaskRequestDTO;
import com.vg.task.domain.dto.TaskResponseDTO;
import com.vg.task.domain.model.Task;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Collections;

@Component
public class TaskMapper {
    
    public Task toDomain(TaskRequestDTO dto) {
        if (dto == null) return null;
        
        OffsetDateTime now = OffsetDateTime.now();
        
        return Task.builder()
                .title(dto.title())
                .description(dto.description())
                .instructions(dto.instructions() != null ? dto.instructions() : "")
                .classId(dto.classId())
                .criterionId(dto.criterionId())
                .pointsValue(dto.pointsValue() != null ? dto.pointsValue() : 0.0)
                .dueDate(dto.dueDate())
                .scheduledPublishDate(dto.scheduledPublishDate())
                .scheduledCloseDate(dto.scheduledCloseDate())
                .status("draft")
                .isDeleted(false)
                .createdBy(dto.createdBy())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
    
    public TaskResponseDTO toResponse(Task task) {
        if (task == null) return null;
        
        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getInstructions(),
                task.getClassId(),
                task.getCriterionId(),
                task.getPointsValue(),
                task.getDueDate(),
                task.getScheduledPublishDate(),
                task.getScheduledCloseDate(),
                task.getStatus(),
                task.getIsDeleted(),
                task.getCreatedBy(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                task.getDeletedAt()
        );
    }
}
