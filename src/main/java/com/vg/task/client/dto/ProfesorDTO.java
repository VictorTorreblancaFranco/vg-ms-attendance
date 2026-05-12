package com.vg.task.client.dto;

public class ProfesorDTO {
    private Integer id;
    private Integer personaId;
    private String numeroEmpleado;
    private String especialidad;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getPersonaId() { return personaId; }
    public void setPersonaId(Integer personaId) { this.personaId = personaId; }
    public String getNumeroEmpleado() { return numeroEmpleado; }
    public void setNumeroEmpleado(String numeroEmpleado) { this.numeroEmpleado = numeroEmpleado; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
}
