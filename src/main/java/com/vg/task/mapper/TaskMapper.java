package com.vg.task.mapper;

import com.vg.task.model.dto.TaskRequestDTO;
import com.vg.task.model.dto.TaskResponseDTO;
import com.vg.task.model.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskResponseDTO toResponseDTO(Task task) {
        if (task == null) return null;
        
        TaskResponseDTO dto = new TaskResponseDTO(
            task.getId(),
            task.getClassId(),
            task.getCriterionId(),
            task.getTitle(),
            task.getDescription(),
            task.getInstructions(),
            task.getAssignmentDate(),
            task.getDueDate(),
            task.getPointsValue(),
            task.getAllowedAttempts(),
            task.getIsGroupTask(),
            task.getVisibleToParents(),
            task.getStatus(),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
        return dto;
    }

    public Task toEntity(TaskRequestDTO request) {
        if (request == null) return null;
        
        Task task = new Task();
        task.setClassId(request.classId());
        task.setCriterionId(request.criterionId());
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setInstructions(request.instructions());
        task.setDueDate(request.dueDate());
        task.setPointsValue(request.pointsValue() != null ? request.pointsValue() : 0.0);
        task.setAllowedAttempts(request.allowedAttempts() != null ? request.allowedAttempts() : 1);
        task.setIsGroupTask(request.isGroupTask() != null ? request.isGroupTask() : false);
        task.setVisibleToParents(request.visibleToParents() != null ? request.visibleToParents() : true);
        
        return task;
    }
}
