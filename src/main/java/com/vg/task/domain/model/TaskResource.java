package com.vg.task.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("task_resources")
public class TaskResource {

    @Id
    private Long id;

    @Column("task_id")
    private Long taskId;

    private String type;
    private String name;
    private String url;

    @Column("size_kb")
    private Integer sizeKb;

    @Column("created_by")
    private Integer createdBy;

    @Column("created_at")
    private OffsetDateTime createdAt;
}
