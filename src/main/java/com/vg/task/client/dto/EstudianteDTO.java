package com.vg.task.client.dto;

public class EstudianteDTO {
    private Integer id;
    private Integer personaId;
    private Integer gradoId;
    private Integer anoAcademicoId;
    private String numeroMatricula;
    private String estadoAcademico;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getPersonaId() { return personaId; }
    public void setPersonaId(Integer personaId) { this.personaId = personaId; }
    public Integer getGradoId() { return gradoId; }
    public void setGradoId(Integer gradoId) { this.gradoId = gradoId; }
    public Integer getAnoAcademicoId() { return anoAcademicoId; }
    public void setAnoAcademicoId(Integer anoAcademicoId) { this.anoAcademicoId = anoAcademicoId; }
    public String getNumeroMatricula() { return numeroMatricula; }
    public void setNumeroMatricula(String numeroMatricula) { this.numeroMatricula = numeroMatricula; }
    public String getEstadoAcademico() { return estadoAcademico; }
    public void setEstadoAcademico(String estadoAcademico) { this.estadoAcademico = estadoAcademico; }
}
