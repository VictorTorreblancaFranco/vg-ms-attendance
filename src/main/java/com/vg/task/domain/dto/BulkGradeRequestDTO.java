package com.vg.task.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkGradeRequestDTO {
    private MultipartFile file;
    private Integer gradedBy;
    private Long taskId; // opcional, si el Excel no trae task_id
}
