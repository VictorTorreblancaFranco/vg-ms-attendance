package com.vg.attendance.domain.service.impl;

import com.vg.attendance.domain.exception.BusinessException;
import com.vg.attendance.domain.model.Attendance;
import com.vg.attendance.domain.service.AttendanceDomainService;
import com.vg.attendance.domain.valueobject.AttendanceStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.time.LocalTime;
import java.time.Duration;

@Component
public class AttendanceDomainServiceImpl implements AttendanceDomainService {
    
    @Override
    public Mono<Attendance> validateAttendance(Attendance attendance) {
        AttendanceStatus status = AttendanceStatus.fromCode(attendance.getEstado());
        
        if (status == AttendanceStatus.TARDANZA && attendance.getHoraLlegada() == null) {
            return Mono.error(new BusinessException("La tardanza requiere hora de llegada"));
        }
        
        if (status == AttendanceStatus.JUSTIFICADO &&
                (attendance.getJustificacionNota() == null || attendance.getJustificacionNota().isBlank())) {
            return Mono.error(new BusinessException("La justificación requiere una nota"));
        }
        
        return Mono.just(attendance);
    }
    
    @Override
    public int calculateLateMinutes(LocalTime arrivalTime, LocalTime classStartTime) {
        if (arrivalTime == null || classStartTime == null) return 0;
        if (arrivalTime.isBefore(classStartTime)) return 0;
        return (int) Duration.between(classStartTime, arrivalTime).toMinutes();
    }
    
    @Override
    public String getStatusDescription(String statusCode) {
        return AttendanceStatus.fromCode(statusCode).getDescription();
    }
}
