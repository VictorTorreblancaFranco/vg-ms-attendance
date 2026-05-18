package com.vg.task.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("task_files")
public class TaskFile {

    @Id
    private Long id;

    @Column("task_id")
    private Long taskId;

    @Column("file_name")
    private String fileName;

    @Column("file_url")
    private String fileUrl;

    @Column("file_type")
    private String fileType;

    @Column("file_size_kb")
    private Integer fileSizeKb;

    @Column("created_at")
    private OffsetDateTime createdAt;
}
