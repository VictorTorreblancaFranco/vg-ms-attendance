package com.vg.task.service.impl;

import com.vg.task.client.AcademicClient;
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
    private final AcademicClient academicClient;

    public TaskServiceImpl(TaskRepository taskRepository, TaskMapper taskMapper, AcademicClient academicClient) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.academicClient = academicClient;
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
        // Validar que la clase existe en academic
        academicClient.validarClase(request.classId())
            .blockOptional()
            .orElseThrow(() -> new ResourceNotFoundException("Clase no existe con ID: " + request.classId()));
        
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
        
        // Validar clase si cambió
        if (!existingTask.getClassId().equals(request.classId())) {
            academicClient.validarClase(request.classId())
                .blockOptional()
                .orElseThrow(() -> new ResourceNotFoundException("Clase no existe con ID: " + request.classId()));
        }
        
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
        
        if (!"draft".equals(task.getStatus())) {
            throw new IllegalStateException("Solo se pueden publicar tareas en estado BORRADOR. Estado actual: " + task.getStatus());
        }
        
        task.setStatus("published");
        task.setUpdatedAt(OffsetDateTime.now());
        Task saved = taskRepository.save(task);
        return taskMapper.toResponseDTO(saved);
    }

    @Override
    public TaskResponseDTO close(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        
        if (!"published".equals(task.getStatus())) {
            throw new IllegalStateException("Solo se pueden cerrar tareas en estado PUBLICADA. Estado actual: " + task.getStatus());
        }
        
        task.setStatus("closed");
        task.setUpdatedAt(OffsetDateTime.now());
        Task saved = taskRepository.save(task);
        return taskMapper.toResponseDTO(saved);
    }

    @Override
    public TaskResponseDTO reactivate(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        
        if ("closed".equals(task.getStatus())) {
            task.setStatus("published");
            task.setUpdatedAt(OffsetDateTime.now());
            Task saved = taskRepository.save(task);
            return taskMapper.toResponseDTO(saved);
        } else if ("draft".equals(task.getStatus())) {
            throw new IllegalStateException("La tarea está en BORRADOR, use PUBLICAR en lugar de REACTIVAR");
        } else {
            throw new IllegalStateException("No se puede reactivar una tarea en estado: " + task.getStatus());
        }
    }
}
