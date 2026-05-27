package com.vg.task.domain.dto;

import java.util.List;

public record TeacherSubjectsDTO(
    Integer teacherId,
    String teacherName,
    List<SubjectDTO> subjects
) {
    public record SubjectDTO(
        Integer subjectId,
        String subjectName,
        Integer gradeId,
        String gradeName,
        Integer classId
    ) {}
}
