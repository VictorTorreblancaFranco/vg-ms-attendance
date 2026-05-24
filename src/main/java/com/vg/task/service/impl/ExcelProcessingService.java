package com.vg.task.service.impl;

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
    public Mono<ExcelProcessResult> processExcel(MultipartFile file) {
        return Mono.fromCallable(() -> {
            List<ExcelGradeRowDTO> validRows = new ArrayList<>();
            List<ExcelError> errors = new ArrayList<>();
            try (InputStream inputStream = file.getInputStream()) {
                Workbook workbook = WorkbookFactory.create(inputStream);
                for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                    Sheet sheet = workbook.getSheetAt(sheetIndex);
                    for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) continue;
                        try {
                            ExcelGradeRowDTO dto = parseRow(row);
                            if (dto.getStudentId() != null) validRows.add(dto);
                        } catch (Exception e) {
                            errors.add(new ExcelError(sheet.getSheetName(), i+1, "", e.getMessage()));
                        }
                    }
                }
                return new ExcelProcessResult(validRows, errors);
            }
        });
    }
    private ExcelGradeRowDTO parseRow(Row row) {
        ExcelGradeRowDTO.ExcelGradeRowDTOBuilder builder = ExcelGradeRowDTO.builder();
        if (row.getCell(0) != null) builder.studentId((int) row.getCell(0).getNumericCellValue());
        if (row.getCell(1) != null) builder.taskId((long) row.getCell(1).getNumericCellValue());
        if (row.getCell(2) != null) builder.grade(row.getCell(2).getNumericCellValue());
        if (row.getCell(3) != null) builder.observations(row.getCell(3).getStringCellValue());
        if (row.getCell(4) != null) builder.justification(row.getCell(4).getStringCellValue());
        if (row.getCell(5) != null) builder.isLate("SI".equalsIgnoreCase(row.getCell(5).getStringCellValue()));
        return builder.build();
    }
    public record ExcelProcessResult(List<ExcelGradeRowDTO> validRows, List<ExcelError> errors) {}
    public record ExcelError(String sheet, int row, String field, String message) {}
}
