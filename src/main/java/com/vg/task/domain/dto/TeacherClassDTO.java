package com.vg.task.domain.dto;

public record TeacherClassDTO(
    Integer classId,
    Integer subjectId,
    String subjectName,
    Integer gradeId,
    String gradeName,
    Boolean active
) {}
