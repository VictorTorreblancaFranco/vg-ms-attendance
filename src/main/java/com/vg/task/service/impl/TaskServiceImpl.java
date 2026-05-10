package com.vg.task.service.impl;

import com.vg.task.exception.ResourceNotFoundException;
import com.vg.task.model.dto.TaskRequestDTO;
import com.vg.task.model.dto.TaskResponseDTO;
import com.vg.task.model.entity.Task;
import com.vg.task.repository.TaskRepository;
import com.vg.task.service.TaskService;
import com.vg.task.mapper.TaskMapper;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public TaskServiceImpl(TaskRepository taskRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    @Override
    public List<TaskResponseDTO> findAll() {
        return taskRepository.findAll().stream()
            .map(taskMapper::toResponseDTO)
            .collect(Collectors.toList());
    }

    @Override
    public TaskResponseDTO findById(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return taskMapper.toResponseDTO(task);
    }

    @Override
    public TaskResponseDTO create(TaskRequestDTO request) {
        Task task = taskMapper.toEntity(request);
        task.setAssignmentDate(LocalDate.now());
        task.setStatus("draft");
        task.setCreatedAt(OffsetDateTime.now());
        task.setUpdatedAt(OffsetDateTime.now());
        
        Task saved = taskRepository.save(task);
        return taskMapper.toResponseDTO(saved);
    }

    @Override
    public TaskResponseDTO update(Long id, TaskRequestDTO request) {
        Task existingTask = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        
        Task updatedTask = taskMapper.toEntity(request);
        updatedTask.setId(id);
        updatedTask.setAssignmentDate(existingTask.getAssignmentDate());
        updatedTask.setStatus(existingTask.getStatus());
        updatedTask.setCreatedAt(existingTask.getCreatedAt());
        updatedTask.setUpdatedAt(OffsetDateTime.now());
        
        Task saved = taskRepository.save(updatedTask);
        return taskMapper.toResponseDTO(saved);
    }

    @Override
    public void delete(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        taskRepository.delete(task);
    }

    @Override
    public List<TaskResponseDTO> findByClassId(Integer classId) {
        return taskRepository.findByClassId(classId).stream()
            .map(taskMapper::toResponseDTO)
            .collect(Collectors.toList());
    }

    @Override
    public List<TaskResponseDTO> findByStatus(String status) {
        return taskRepository.findByStatus(status).stream()
            .map(taskMapper::toResponseDTO)
            .collect(Collectors.toList());
    }

    @Override
    public TaskResponseDTO publish(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        task.setStatus("published");
        task.setUpdatedAt(OffsetDateTime.now());
        Task saved = taskRepository.save(task);
        return taskMapper.toResponseDTO(saved);
    }

    @Override
    public TaskResponseDTO close(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        task.setStatus("closed");
        task.setUpdatedAt(OffsetDateTime.now());
        Task saved = taskRepository.save(task);
        return taskMapper.toResponseDTO(saved);
    }
}
