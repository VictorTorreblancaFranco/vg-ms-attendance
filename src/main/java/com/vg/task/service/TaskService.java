package com.vg.task.service;

import com.vg.task.model.dto.TaskRequestDTO;
import com.vg.task.model.dto.TaskResponseDTO;
import java.util.List;

public interface TaskService {
    List<TaskResponseDTO> findAll();
    TaskResponseDTO findById(Long id);
    TaskResponseDTO create(TaskRequestDTO request);
    TaskResponseDTO update(Long id, TaskRequestDTO request);
    void delete(Long id);
    List<TaskResponseDTO> findByClassId(Integer classId);
    List<TaskResponseDTO> findByStatus(String status);
    TaskResponseDTO publish(Long id);
    TaskResponseDTO close(Long id);
}
