package com.vg.task.service.impl;

import com.vg.task.client.StudentClient;
import com.vg.task.domain.dto.AttendanceBulkDTO;
import com.vg.task.domain.dto.AttendanceDTO;
import com.vg.task.domain.dto.PageResponseDTO;
import com.vg.task.domain.model.Attendance;
import com.vg.task.domain.model.exceptions.BadRequestException;
import com.vg.task.domain.model.exceptions.NotFoundException;
import com.vg.task.repository.AttendanceRepository;
import com.vg.task.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentClient studentClient;
    private static final Set<String> VALID_STATUS = Set.of("A", "F", "J");

    @Override
    public Flux<AttendanceDTO> findByClassId(Integer classId) {
        return attendanceRepository.findByClassId(classId)
                .flatMap(this::enrichWithStudentInfo);
    }

    public Mono<PageResponseDTO<AttendanceDTO>> findByClassIdPaged(Integer classId, int page, int size) {
        return attendanceRepository.findByClassId(classId)
                .collectList()
                .flatMap(list -> {
                    int total = list.size();
                    int start = page * size;
                    int end = Math.min(start + size, total);
                    if (start >= total) {
                        return Mono.just(PageResponseDTO.of(new ArrayList<>(), page, size, total));
                    }
                    List<Attendance> paged = list.subList(start, end);
                    return Flux.fromIterable(paged)
                            .flatMap(this::enrichWithStudentInfo)
                            .collectList()
                            .map(dtos -> PageResponseDTO.of(dtos, page, size, total));
                });
    }

    @Override
    public Flux<AttendanceDTO> findByClassIdAndDate(Integer classId, LocalDate date) {
        return attendanceRepository.findByClassIdAndDate(classId, date)
                .flatMap(this::enrichWithStudentInfo);
    }

    @Override
    public Flux<AttendanceDTO> findByStudentId(Integer studentId) {
        return attendanceRepository.findByStudentId(studentId)
                .flatMap(this::enrichWithStudentInfo);
    }

    public Mono<PageResponseDTO<AttendanceDTO>> findByStudentIdPaged(Integer studentId, int page, int size) {
        return attendanceRepository.findByStudentId(studentId)
                .collectList()
                .flatMap(list -> {
                    int total = list.size();
                    int start = page * size;
                    int end = Math.min(start + size, total);
                    if (start >= total) {
                        return Mono.just(PageResponseDTO.of(new ArrayList<>(), page, size, total));
                    }
                    List<Attendance> paged = list.subList(start, end);
                    return Flux.fromIterable(paged)
                            .flatMap(this::enrichWithStudentInfo)
                            .collectList()
                            .map(dtos -> PageResponseDTO.of(dtos, page, size, total));
                });
    }

    @Override
    public Mono<AttendanceDTO> save(AttendanceDTO dto) {
        return validateStatus(dto.status())
            .then(validateStudentExists(dto.studentId()))
            .then(Mono.defer(() -> {
                Attendance attendance = Attendance.builder()
                        .classId(dto.classId())
                        .studentId(dto.studentId())
                        .date(dto.date())
                        .status(dto.status())
                        .observation(dto.observation())
                        .createdBy(dto.createdBy() != null ? dto.createdBy() : 1)
                        .build();
                return attendanceRepository.save(attendance);
            }))
            .flatMap(this::enrichWithStudentInfo);
    }

    @Override
    public Mono<AttendanceDTO> update(Long id, AttendanceDTO dto) {
        return attendanceRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Attendance not found: " + id)))
                .flatMap(existing -> {
                    existing.setClassId(dto.classId());
                    existing.setStudentId(dto.studentId());
                    existing.setDate(dto.date());
                    existing.setStatus(dto.status());
                    existing.setObservation(dto.observation());
                    return attendanceRepository.save(existing);
                })
                .flatMap(this::enrichWithStudentInfo);
    }

    @Override
    public Mono<Void> delete(Long id) {
        return attendanceRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Attendance not found: " + id)))
                .flatMap(attendanceRepository::delete);
    }

    @Override
    public Mono<byte[]> getTemplate() {
        return Mono.fromCallable(() -> {
            try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("Plantilla Asistencias");
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("studentId");
                header.createCell(1).setCellValue("status");
                header.createCell(2).setCellValue("observation");
                workbook.write(out);
                return out.toByteArray();
            } catch (Exception e) {
                throw new RuntimeException("Error generating template", e);
            }
        });
    }

    @Override
    public Mono<byte[]> exportToExcel(Integer classId, LocalDate date) {
        return attendanceRepository.findByClassIdAndDate(classId, date)
                .flatMap(this::enrichWithStudentInfo)
                .collectList()
                .map(attendances -> {
                    try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                        Sheet sheet = workbook.createSheet("Asistencias " + date);
                        Row header = sheet.createRow(0);
                        header.createCell(0).setCellValue("Estudiante");
                        header.createCell(1).setCellValue("Código");
                        header.createCell(2).setCellValue("Estado");
                        header.createCell(3).setCellValue("Observación");
                        
                        int rowNum = 1;
                        for (AttendanceDTO a : attendances) {
                            Row row = sheet.createRow(rowNum++);
                            row.createCell(0).setCellValue(a.studentName() != null ? a.studentName() : "");
                            row.createCell(1).setCellValue(a.studentCode() != null ? a.studentCode() : "");
                            row.createCell(2).setCellValue(a.status());
                            row.createCell(3).setCellValue(a.observation() != null ? a.observation() : "");
                        }
                        workbook.write(out);
                        return out.toByteArray();
                    } catch (Exception e) {
                        throw new RuntimeException("Error exporting to Excel", e);
                    }
                });
    }

    // NUEVO: Método para carga masiva con UPSERT (actualiza si existe, inserta si no)
    @Override
    public Mono<List<AttendanceDTO>> saveBulk(AttendanceBulkDTO bulkDTO) {
        log.info("Guardando {} asistencias para clase {} en fecha {}", 
            bulkDTO.attendances().size(), bulkDTO.classId(), bulkDTO.date());
        
        if (bulkDTO.attendances() == null || bulkDTO.attendances().isEmpty()) {
            return Mono.error(new BadRequestException("La lista de asistencias no puede estar vacía"));
        }
        
        // Validar status de cada item
        for (AttendanceBulkDTO.BulkAttendanceItem item : bulkDTO.attendances()) {
            if (!VALID_STATUS.contains(item.status())) {
                return Mono.error(new BadRequestException("Status inválido: " + item.status() + ". Debe ser A, F o J"));
            }
        }
        
        // Procesar cada asistencia: buscar si existe, si existe actualizar, si no insertar
        return Flux.fromIterable(bulkDTO.attendances())
            .flatMap(item -> 
                // Buscar si ya existe una asistencia para este estudiante, clase y fecha
                attendanceRepository.findByClassIdAndStudentIdAndDate(
                    bulkDTO.classId(), item.studentId(), bulkDTO.date()
                )
                .flatMap(existing -> {
                    // Actualizar asistencia existente
                    existing.setStatus(item.status());
                    existing.setObservation(item.observation());
                    log.debug("Actualizando asistencia existente para estudiante {}", item.studentId());
                    return attendanceRepository.save(existing);
                })
                .switchIfEmpty(
                    // Crear nueva asistencia
                    validateStudentExists(item.studentId())
                        .then(Mono.defer(() -> {
                            Attendance attendance = Attendance.builder()
                                    .classId(bulkDTO.classId())
                                    .studentId(item.studentId())
                                    .date(bulkDTO.date())
                                    .status(item.status())
                                    .observation(item.observation())
                                    .createdBy(1)
                                    .build();
                            log.debug("Creando nueva asistencia para estudiante {}", item.studentId());
                            return attendanceRepository.save(attendance);
                        }))
                )
            )
            .flatMap(this::enrichWithStudentInfo)
            .collectList()
            .doOnSuccess(list -> log.info("Asistencias procesadas exitosamente: {} registros", list.size()));
    }

    private Mono<Void> validateStatus(String status) {
        if (!VALID_STATUS.contains(status)) {
            return Mono.error(new BadRequestException("Status inválido: " + status + ". Debe ser A, F o J"));
        }
        return Mono.empty();
    }

    private Mono<Void> validateStudentExists(Integer studentId) {
        return studentClient.validateStudent(studentId)
            .flatMap(exists -> {
                if (Boolean.TRUE.equals(exists)) {
                    return Mono.empty();
                }
                return Mono.error(new NotFoundException("Estudiante no encontrado: " + studentId));
            });
    }

    private Mono<AttendanceDTO> enrichWithStudentInfo(Attendance attendance) {
        return studentClient.getStudentInfo(attendance.getStudentId())
            .map(student -> new AttendanceDTO(
                attendance.getId(),
                attendance.getClassId(),
                attendance.getStudentId(),
                attendance.getDate(),
                attendance.getStatus(),
                attendance.getObservation(),
                attendance.getCreatedBy(),
                student != null ? student.nombre() : "Desconocido",
                student != null ? student.studentCode() : ""
            ))
            .defaultIfEmpty(new AttendanceDTO(
                attendance.getId(),
                attendance.getClassId(),
                attendance.getStudentId(),
                attendance.getDate(),
                attendance.getStatus(),
                attendance.getObservation(),
                attendance.getCreatedBy(),
                "Desconocido",
                ""
            ));
    }
}
