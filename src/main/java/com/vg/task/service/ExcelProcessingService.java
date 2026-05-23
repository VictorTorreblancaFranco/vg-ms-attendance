package com.vg.task.service;

import com.vg.task.domain.dto.excel.ExcelGradeRowDTO;
import com.vg.task.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ExcelProcessingService {

    public Mono<List<ExcelGradeRowDTO>> processExcel(MultipartFile file) {
        return Mono.fromCallable(() -> {
            validateFile(file);
            
            List<ExcelGradeRowDTO> rows = new ArrayList<>();
            
            try (InputStream inputStream = file.getInputStream()) {
                Workbook workbook = WorkbookFactory.create(inputStream);
                Sheet sheet = workbook.getSheetAt(0);
                
                // Saltar encabezado (fila 0)
                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    
                    ExcelGradeRowDTO dto = parseRow(row);
                    if (dto.getStudentId() != null && dto.getTaskId() != null) {
                        rows.add(dto);
                    }
                }
                workbook.close();
                
                if (rows.isEmpty()) {
                    throw new BadRequestException("El Excel no contiene datos válidos");
                }
                
                log.info("✅ Procesadas {} filas del Excel", rows.size());
                return rows;
            } catch (Exception e) {
                log.error("Error procesando Excel: {}", e.getMessage());
                throw new BadRequestException("Error al leer el archivo Excel: " + e.getMessage());
            }
        });
    }
    
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("El archivo Excel es requerido");
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            throw new BadRequestException("Formato de archivo no soportado. Use .xlsx o .xls");
        }
        
        if (file.getSize() > 5 * 1024 * 1024) { // 5MB
            throw new BadRequestException("El archivo no puede superar los 5MB");
        }
    }
    
    private ExcelGradeRowDTO parseRow(Row row) {
        ExcelGradeRowDTO.ExcelGradeRowDTOBuilder builder = ExcelGradeRowDTO.builder();
        
        // Columna A: student_id
        Cell studentIdCell = row.getCell(0);
        if (studentIdCell != null) {
            if (studentIdCell.getCellType() == CellType.NUMERIC) {
                builder.studentId((int) studentIdCell.getNumericCellValue());
            } else if (studentIdCell.getCellType() == CellType.STRING) {
                builder.studentId(Integer.parseInt(studentIdCell.getStringCellValue()));
            }
        }
        
        // Columna B: task_id
        Cell taskIdCell = row.getCell(1);
        if (taskIdCell != null) {
            if (taskIdCell.getCellType() == CellType.NUMERIC) {
                builder.taskId((long) taskIdCell.getNumericCellValue());
            } else if (taskIdCell.getCellType() == CellType.STRING) {
                builder.taskId(Long.parseLong(taskIdCell.getStringCellValue()));
            }
        }
        
        // Columna C: grade
        Cell gradeCell = row.getCell(2);
        if (gradeCell != null && gradeCell.getCellType() == CellType.NUMERIC) {
            double grade = gradeCell.getNumericCellValue();
            if (grade < 0 || grade > 20) {
                throw new BadRequestException("Nota inválida para estudiante " + builder.build().getStudentId() + ": " + grade);
            }
            builder.grade(grade);
        }
        
        // Columna D: observations
        Cell obsCell = row.getCell(3);
        if (obsCell != null) {
            builder.observations(getCellValueAsString(obsCell));
        }
        
        // Columna E: justification
        Cell justificationCell = row.getCell(4);
        if (justificationCell != null) {
            builder.justification(getCellValueAsString(justificationCell));
        }
        
        // Columna F: is_late (SI/NO)
        Cell lateCell = row.getCell(5);
        if (lateCell != null) {
            String value = getCellValueAsString(lateCell);
            builder.isLate("SI".equalsIgnoreCase(value) || "TRUE".equalsIgnoreCase(value));
        } else {
            builder.isLate(false);
        }
        
        return builder.build();
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }
}
