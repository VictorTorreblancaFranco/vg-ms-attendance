package com.vg.task.repository;

import com.vg.task.domain.model.Attendance;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.LocalDate;

@Repository
public interface AttendanceRepository extends ReactiveCrudRepository<Attendance, Long> {
    Flux<Attendance> findByClassId(Integer classId);
    Flux<Attendance> findByClassIdAndDate(Integer classId, LocalDate date);
    Flux<Attendance> findByStudentId(Integer studentId);
    Mono<Attendance> findByClassIdAndStudentIdAndDate(Integer classId, Integer studentId, LocalDate date);
}
