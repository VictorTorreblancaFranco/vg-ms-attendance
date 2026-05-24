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
@Table("notifications")
public class Notification {

    @Id
    private Long id;

    @Column("user_id")
    private Integer userId;

    @Column("task_id")
    private Long taskId;

    private String type;
    private String title;
    private String message;

    @Column("is_read")
    private Boolean isRead;

    @Column("created_at")
    private OffsetDateTime createdAt;
}
