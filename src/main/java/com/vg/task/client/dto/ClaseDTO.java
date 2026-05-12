package com.vg.task.client.dto;

public class ClaseDTO {
    private Integer id;
    private Integer profesorId;
    private Integer gradoId;
    private Integer materiaId;
    private Integer anoAcademicoId;
    private Boolean activa;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getProfesorId() { return profesorId; }
    public void setProfesorId(Integer profesorId) { this.profesorId = profesorId; }
    public Integer getGradoId() { return gradoId; }
    public void setGradoId(Integer gradoId) { this.gradoId = gradoId; }
    public Integer getMateriaId() { return materiaId; }
    public void setMateriaId(Integer materiaId) { this.materiaId = materiaId; }
    public Integer getAnoAcademicoId() { return anoAcademicoId; }
    public void setAnoAcademicoId(Integer anoAcademicoId) { this.anoAcademicoId = anoAcademicoId; }
    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }
}
