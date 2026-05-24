package com.vg.task.service;

import com.vg.task.domain.model.Task;
import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private static final String[] HEADERS = {
        "ID", "Título", "Descripción", "Instrucciones", "ID Clase",
        "Puntaje", "Fecha Entrega", "Fecha Publicación", "Fecha Cierre",
        "Estado", "Eliminado", "Creado Por", "Fecha Creación", "Fecha Actualización"
    };

    public Mono<byte[]> exportToCsv(List<Task> tasks) {
        return Mono.fromCallable(() -> {
            StringWriter stringWriter = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(stringWriter);
            csvWriter.writeNext(HEADERS);
            for (Task task : tasks) {
                csvWriter.writeNext(taskToRow(task));
            }
            csvWriter.close();
            return stringWriter.toString().getBytes();
        });
    }

    public Mono<byte[]> exportToExcel(List<Task> tasks) {
        return Mono.fromCallable(() -> {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Tareas");
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }
            
            int rowNum = 1;
            for (Task task : tasks) {
                Row row = sheet.createRow(rowNum++);
                String[] rowData = taskToRow(task);
                for (int i = 0; i < rowData.length; i++) {
                    row.createCell(i).setCellValue(rowData[i]);
                }
            }
            
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();
        });
    }
    
    private String[] taskToRow(Task task) {
        return new String[]{
            String.valueOf(task.getId()),
            task.getTitle() != null ? task.getTitle() : "",
            task.getDescription() != null ? task.getDescription() : "",
            task.getInstructions() != null ? task.getInstructions() : "",
            String.valueOf(task.getClassId()),
            String.valueOf(task.getPointsValue()),
            task.getDueDate() != null ? task.getDueDate().format(DATE_FORMATTER) : "",
            task.getScheduledPublishDate() != null ? task.getScheduledPublishDate().format(DATE_FORMATTER) : "",
            task.getScheduledCloseDate() != null ? task.getScheduledCloseDate().format(DATE_FORMATTER) : "",
            task.getStatus() != null ? task.getStatus() : "",
            task.getIsDeleted() != null && task.getIsDeleted() ? "Sí" : "No",
            String.valueOf(task.getCreatedBy()),
            task.getCreatedAt() != null ? task.getCreatedAt().format(DATE_FORMATTER) : "",
            task.getUpdatedAt() != null ? task.getUpdatedAt().format(DATE_FORMATTER) : ""
        };
    }
}
