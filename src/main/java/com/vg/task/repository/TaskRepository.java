package com.vg.task.repository;

import com.vg.task.model.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByClassId(Integer classId);
    List<Task> findByStatus(String status);
    List<Task> findByClassIdAndStatus(Integer classId, String status);
}
