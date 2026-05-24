package com.vg.task.service.impl;

import com.vg.task.client.StudentClient;
import com.vg.task.domain.dto.AttendanceDTO;
import com.vg.task.domain.dto.AttendanceBulkDTO;
import com.vg.task.domain.model.Attendance;
import com.vg.task.exception.BadRequestException;
import com.vg.task.exception.NotFoundException;
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
                        .createdBy(dto.createdBy())
                        .createdAt(java.time.OffsetDateTime.now())
                        .updatedAt(java.time.OffsetDateTime.now())
                        .build();
                return attendanceRepository.save(attendance);
            }))
            .flatMap(this::enrichWithStudentInfo);
    }

    @Override
    public Mono<AttendanceDTO> update(Long id, AttendanceDTO dto) {
        return attendanceRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Attendance not found: " + id)))
                .flatMap(existing -> validateStatus(dto.status())
                    .then(Mono.defer(() -> {
                        existing.setStatus(dto.status());
                        existing.setObservation(dto.observation());
                        existing.setUpdatedAt(java.time.OffsetDateTime.now());
                        return attendanceRepository.save(existing);
                    })))
                .flatMap(this::enrichWithStudentInfo);
    }

    @Override
    public Mono<Void> delete(Long id) {
        return attendanceRepository.deleteById(id);
    }

    @Override
    public Mono<List<AttendanceDTO>> bulkSave(List<AttendanceBulkDTO> list, Integer classId, LocalDate date) {
        return Flux.fromIterable(list)
                .concatMap(dto -> {
                    Attendance attendance = Attendance.builder()
                            .classId(dto.getClassId() != null ? dto.getClassId() : classId)
                            .studentId(dto.getStudentId())
                            .date(dto.getDate() != null ? dto.getDate() : date)
                            .status(dto.getStatus())
                            .observation(dto.getObservation())
                            .createdAt(java.time.OffsetDateTime.now())
                            .updatedAt(java.time.OffsetDateTime.now())
                            .build();
                    return attendanceRepository.save(attendance);
                })
                .collectList()
                .flatMapMany(Flux::fromIterable)
                .flatMap(this::enrichWithStudentInfo)
                .collectList();
    }

    @Override
    public Mono<byte[]> exportToExcel(Integer classId, LocalDate date) {
        return findByClassIdAndDate(classId, date)
                .collectList()
                .map(list -> generateExcel(list, classId, date));
    }

    @Override
    public Mono<byte[]> getTemplate() {
        return Mono.fromCallable(() -> {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Plantilla Asistencia");
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            String[] headers = {"student_id", "class_id", "date", "status", "observation"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("123");
            exampleRow.createCell(1).setCellValue("1");
            exampleRow.createCell(2).setCellValue("2024-01-15");
            exampleRow.createCell(3).setCellValue("A");
            exampleRow.createCell(4).setCellValue("");
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();
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
                        student != null ? student.studentCode() : "N/A"
                ))
                .switchIfEmpty(Mono.just(new AttendanceDTO(
                        attendance.getId(),
                        attendance.getClassId(),
                        attendance.getStudentId(),
                        attendance.getDate(),
                        attendance.getStatus(),
                        attendance.getObservation(),
                        attendance.getCreatedBy(),
                        "Desconocido",
                        "N/A"
                )));
    }

    private Mono<Void> validateStatus(String status) {
        if (!VALID_STATUS.contains(status)) {
            return Mono.error(new BadRequestException("Status inválido. Use A (Asistió), F (Faltó) o J (Justificado)"));
        }
        return Mono.empty();
    }

    private Mono<Void> validateStudentExists(Integer studentId) {
        return studentClient.validateStudent(studentId)
                .flatMap(valid -> valid ? Mono.empty() : Mono.error(new NotFoundException("Student not found: " + studentId)));
    }

    private byte[] generateExcel(List<AttendanceDTO> list, Integer classId, LocalDate date) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Asistencia Clase " + classId);
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            String[] headers = {"ID", "Estudiante", "Código", "Fecha", "Estado", "Observación"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (AttendanceDTO a : list) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(a.id());
                row.createCell(1).setCellValue(a.studentName());
                row.createCell(2).setCellValue(a.studentCode());
                row.createCell(3).setCellValue(a.date().format(formatter));
                String statusText = a.status().equals("A") ? "Asistió" : (a.status().equals("F") ? "Faltó" : "Justificado");
                row.createCell(4).setCellValue(statusText);
                row.createCell(5).setCellValue(a.observation() != null ? a.observation() : "");
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating Excel", e);
        }
    }
}
