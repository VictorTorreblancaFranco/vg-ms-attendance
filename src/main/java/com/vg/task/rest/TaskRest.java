package com.vg.task.rest;

import com.vg.task.model.dto.TaskRequestDTO;
import com.vg.task.model.dto.TaskResponseDTO;
import com.vg.task.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/task/tasks")
public class TaskRest {

    private final TaskService taskService;

    public TaskRest(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDTO>> getAll() {
        return ResponseEntity.ok(taskService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.findById(id));
    }

    @PostMapping
    public ResponseEntity<TaskResponseDTO> create(@Valid @RequestBody TaskRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponseDTO> update(@PathVariable Long id, @Valid @RequestBody TaskRequestDTO request) {
        return ResponseEntity.ok(taskService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<List<TaskResponseDTO>> getByClassId(@PathVariable Integer classId) {
        return ResponseEntity.ok(taskService.findByClassId(classId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TaskResponseDTO>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(taskService.findByStatus(status));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<TaskResponseDTO> publish(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.publish(id));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<TaskResponseDTO> close(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.close(id));
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<TaskResponseDTO> reactivate(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.reactivate(id));
    }
}
