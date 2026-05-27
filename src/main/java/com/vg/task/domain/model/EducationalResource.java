package com.vg.task.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.OffsetDateTime;

@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("educational_resources")
public class EducationalResource {
    @Id
    private Long id;
    private String title;
    private String description;
    private String type;  // pdf, video, link, documento
    private String url;
    @Column("file_path")
    private String filePath;
    @Column("subject_id")
    private Integer subjectId;
    @Column("grade_id")
    private Integer gradeId;
    @Column("created_by")
    private Integer createdBy;
    @Column("is_public")
    private Boolean isPublic;
    @Column("created_at")
    private OffsetDateTime createdAt;
    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
