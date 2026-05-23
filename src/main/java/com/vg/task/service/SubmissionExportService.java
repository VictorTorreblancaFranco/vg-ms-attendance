package com.vg.task.service;

import com.vg.task.domain.model.Submission;
import com.vg.task.domain.model.Task;
import com.vg.task.repository.SubmissionRepository;
import com.vg.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionExportService {

    private final SubmissionRepository submissionRepository;
    private final TaskRepository taskRepository;

    public Mono<byte[]> exportSubmissionsToExcel(Long taskId) {
        return taskRepository.findById(taskId)
            .switchIfEmpty(Mono.error(new RuntimeException("Tarea no encontrada")))
            .flatMap(task -> submissionRepository.findByTaskId(taskId)
                .collectList()
                .map(submissions -> createExcel(task, submissions)));
    }

    private byte[] createExcel(Task task, List<Submission> submissions) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Entregas");

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        String[] headers = {"ID Estudiante", "Presentó", "Nota", "Observaciones", "Entregó Tarde", "Fecha Entrega", "Estado"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Submission sub : submissions) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(sub.getStudentId());
            row.createCell(1).setCellValue(sub.getPresented() != null && sub.getPresented() ? "SÍ" : "NO");
            row.createCell(2).setCellValue(sub.getGrade() != null ? sub.getGrade() : 0);
            row.createCell(3).setCellValue(sub.getObservations() != null ? sub.getObservations() : "");
            row.createCell(4).setCellValue(sub.getIsLate() != null && sub.getIsLate() ? "SÍ" : "NO");
            row.createCell(5).setCellValue(sub.getSubmissionDate().format(formatter));
            row.createCell(6).setCellValue(sub.getStatus());
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("Error creating Excel: {}", e.getMessage());
            throw new RuntimeException("Error al generar Excel", e);
        }
    }

    public Mono<byte[]> getTemplate() {
        return Mono.fromCallable(() -> {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Plantilla Calificaciones");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            String[] headers = {"student_id", "task_id", "grade", "observations", "justification", "is_late"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("123");
            exampleRow.createCell(1).setCellValue("1");
            exampleRow.createCell(2).setCellValue("18");
            exampleRow.createCell(3).setCellValue("Excelente trabajo");
            exampleRow.createCell(4).setCellValue("");
            exampleRow.createCell(5).setCellValue("NO");

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();
        });
    }
}
