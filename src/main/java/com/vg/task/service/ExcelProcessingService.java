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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ExcelProcessingService {

    private static final Map<String, Integer> REQUIRED_COLUMNS = Map.of(
        "student_id", 0,
        "task_id", 1,
        "grade", 2,
        "observations", 3,
        "justification", 4,
        "is_late", 5
    );

    public Mono<ExcelProcessResult> processExcel(MultipartFile file) {
        return Mono.fromCallable(() -> {
            validateFile(file);
            
            List<ExcelGradeRowDTO> validRows = new ArrayList<>();
            List<ExcelError> errors = new ArrayList<>();
            
            try (InputStream inputStream = file.getInputStream()) {
                Workbook workbook = WorkbookFactory.create(inputStream);
                int sheetCount = workbook.getNumberOfSheets();
                
                for (int sheetIndex = 0; sheetIndex < sheetCount; sheetIndex++) {
                    Sheet sheet = workbook.getSheetAt(sheetIndex);
                    String sheetName = sheet.getSheetName();
                    
                    // Validar encabezados
                    Row headerRow = sheet.getRow(0);
                    if (headerRow == null) {
                        errors.add(new ExcelError(sheetName, 0, "ENCABEZADO", "Hoja sin encabezados"));
                        continue;
                    }
                    
                    validateHeaders(headerRow, sheetName, errors);
                    
                    // Procesar filas
                    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) continue;
                        
                        try {
                            ExcelGradeRowDTO dto = parseRowWithValidation(row, sheetName, i);
                            if (dto.getStudentId() != null && dto.getTaskId() != null) {
                                validRows.add(dto);
                            }
                        } catch (Exception e) {
                            errors.add(new ExcelError(sheetName, i + 1, "FILA_" + (i + 1), e.getMessage()));
                        }
                    }
                }
                workbook.close();
                
                if (validRows.isEmpty() && errors.isEmpty()) {
                    throw new BadRequestException("El Excel no contiene datos válidos");
                }
                
                log.info("✅ Procesadas {} filas válidas, {} errores", validRows.size(), errors.size());
                return new ExcelProcessResult(validRows, errors);
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
        
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BadRequestException("El archivo no puede superar los 10MB");
        }
    }
    
    private void validateHeaders(Row headerRow, String sheetName, List<ExcelError> errors) {
        for (Map.Entry<String, Integer> entry : REQUIRED_COLUMNS.entrySet()) {
            String expectedColumn = entry.getKey();
            int index = entry.getValue();
            Cell cell = headerRow.getCell(index);
            
            if (cell == null || cell.getStringCellValue() == null || 
                !cell.getStringCellValue().toLowerCase().equals(expectedColumn)) {
                errors.add(new ExcelError(sheetName, 0, "ENCABEZADO", 
                    "Columna " + index + " debería ser '" + expectedColumn + "'"));
            }
        }
    }
    
    private ExcelGradeRowDTO parseRowWithValidation(Row row, String sheetName, int rowNum) {
        ExcelGradeRowDTO.ExcelGradeRowDTOBuilder builder = ExcelGradeRowDTO.builder();
        List<String> fieldErrors = new ArrayList<>();
        
        // student_id
        try {
            Cell cell = row.getCell(0);
            if (cell != null) {
                if (cell.getCellType() == CellType.NUMERIC) {
                    builder.studentId((int) cell.getNumericCellValue());
                } else if (cell.getCellType() == CellType.STRING) {
                    builder.studentId(Integer.parseInt(cell.getStringCellValue()));
                }
            }
            if (builder.build().getStudentId() == null) {
                fieldErrors.add("student_id es requerido");
            }
        } catch (Exception e) {
            fieldErrors.add("student_id inválido: " + e.getMessage());
        }
        
        // task_id
        try {
            Cell cell = row.getCell(1);
            if (cell != null) {
                if (cell.getCellType() == CellType.NUMERIC) {
                    builder.taskId((long) cell.getNumericCellValue());
                } else if (cell.getCellType() == CellType.STRING) {
                    builder.taskId(Long.parseLong(cell.getStringCellValue()));
                }
            }
        } catch (Exception e) {
            fieldErrors.add("task_id inválido: " + e.getMessage());
        }
        
        // grade
        try {
            Cell cell = row.getCell(2);
            if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                double grade = cell.getNumericCellValue();
                if (grade < 0 || grade > 20) {
                    fieldErrors.add("grade debe estar entre 0 y 20");
                } else {
                    builder.grade(grade);
                }
            }
        } catch (Exception e) {
            fieldErrors.add("grade inválido: " + e.getMessage());
        }
        
        // observations
        Cell obsCell = row.getCell(3);
        if (obsCell != null) {
            builder.observations(getCellValueAsString(obsCell));
        }
        
        // justification
        Cell justificationCell = row.getCell(4);
        if (justificationCell != null) {
            builder.justification(getCellValueAsString(justificationCell));
        }
        
        // is_late
        Cell lateCell = row.getCell(5);
        if (lateCell != null) {
            String value = getCellValueAsString(lateCell);
            builder.isLate("SI".equalsIgnoreCase(value) || "TRUE".equalsIgnoreCase(value) || "SÍ".equalsIgnoreCase(value));
        } else {
            builder.isLate(false);
        }
        
        if (!fieldErrors.isEmpty()) {
            throw new RuntimeException("Errores en fila: " + String.join(", ", fieldErrors));
        }
        
        return builder.build();
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }
    
    public record ExcelProcessResult(List<ExcelGradeRowDTO> validRows, List<ExcelError> errors) {}
    public record ExcelError(String sheet, int row, String field, String message) {}
}
