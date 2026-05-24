package com.vg.task.web.handler;

import com.vg.task.domain.dto.AttendanceDTO;
import com.vg.task.service.impl.AttendanceServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import java.time.LocalDate;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AttendanceHandler {

    private final AttendanceServiceImpl attendanceService;

    public Mono<ServerResponse> findByClassId(ServerRequest request) {
        Integer classId = Integer.parseInt(request.pathVariable("classId"));
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(attendanceService.findByClassId(classId), AttendanceDTO.class);
    }

    public Mono<ServerResponse> findByClassIdAndDate(ServerRequest request) {
        Integer classId = Integer.parseInt(request.pathVariable("classId"));
        LocalDate date = LocalDate.parse(request.pathVariable("date"));
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(attendanceService.findByClassIdAndDate(classId, date), AttendanceDTO.class);
    }

    public Mono<ServerResponse> findByStudentId(ServerRequest request) {
        Integer studentId = Integer.parseInt(request.pathVariable("studentId"));
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(attendanceService.findByStudentId(studentId), AttendanceDTO.class);
    }

    public Mono<ServerResponse> save(ServerRequest request) {
        return request.bodyToMono(AttendanceDTO.class)
                .flatMap(attendanceService::save)
                .flatMap(dto -> ServerResponse.ok().bodyValue(dto));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return request.bodyToMono(AttendanceDTO.class)
                .flatMap(dto -> attendanceService.update(id, dto))
                .flatMap(dto -> ServerResponse.ok().bodyValue(dto));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));
        return attendanceService.delete(id)
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> bulkUpload(ServerRequest request) {
        return request.multipartData()
                .flatMap(parts -> {
                    Integer classId = Integer.parseInt(request.queryParam("classId").orElse("0"));
                    LocalDate date = LocalDate.parse(request.queryParam("date").orElse(LocalDate.now().toString()));
                    return ServerResponse.ok().bodyValue(Map.of("message", "Bulk upload processed", "classId", classId, "date", date.toString()));
                });
    }

    public Mono<ServerResponse> exportToExcel(ServerRequest request) {
        Integer classId = Integer.parseInt(request.pathVariable("classId"));
        LocalDate date = LocalDate.parse(request.pathVariable("date"));
        String filename = "asistencia_clase_" + classId + "_" + date + ".xlsx";
        return attendanceService.exportToExcel(classId, date)
                .flatMap(data -> ServerResponse.ok()
                        .header("Content-Disposition", "attachment; filename=" + filename)
                        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .bodyValue(data));
    }

    public Mono<ServerResponse> getTemplate(ServerRequest request) {
        return attendanceService.getTemplate()
                .flatMap(data -> ServerResponse.ok()
                        .header("Content-Disposition", "attachment; filename=plantilla_asistencia.xlsx")
                        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        .bodyValue(data));
    }
}
