package com.example.RecetaApp.dto;

import java.util.List;

import com.example.RecetaApp.model.Receta;

public class RecetaDTO {
    private Long id;
    private String nombre;
    private String creadorUsername;
    private Integer tiempoPreparacion;
    private String dificultad;
    private String ingredientes;
    private List<String> listaIngredientes;
    private String preparacion;
    private List<String> pasosPreparacion;

    public RecetaDTO(Receta receta) {
        this.id = receta.getId();
        this.nombre = receta.getNombre();
        this.creadorUsername = receta.getCreadoPor() != null ? receta.getCreadoPor().getUsername() : "Anónimo";
        this.tiempoPreparacion = receta.getTiempoPreparacion();
        this.dificultad = receta.getDificultad();
        this.ingredientes = receta.getIngredientes();
        this.listaIngredientes = receta.getListaIngredientes();
        this.preparacion = receta.getPreparacion();
        this.pasosPreparacion = receta.getPasosPreparacion();
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCreadorUsername() {
        return creadorUsername;
    }

    public void setCreadorUsername(String creadorUsername) {
        this.creadorUsername = creadorUsername;
    }

    public Integer getTiempoPreparacion() {
        return tiempoPreparacion;
    }

    public void setTiempoPreparacion(Integer tiempoPreparacion) {
        this.tiempoPreparacion = tiempoPreparacion;
    }

    public String getDificultad() {
        return dificultad;
    }

    public void setDificultad(String dificultad) {
        this.dificultad = dificultad;
    }

    public String getIngredientes() {
        return ingredientes;
    }

    public void setIngredientes(String ingredientes) {
        this.ingredientes = ingredientes;
    }

    public List<String> getListaIngredientes() {
        return listaIngredientes;
    }

    public void setListaIngredientes(List<String> listaIngredientes) {
        this.listaIngredientes = listaIngredientes;
    }

    public String getPreparacion() {
        return preparacion;
    }

    public void setPreparacion(String preparacion) {
        this.preparacion = preparacion;
    }

    public List<String> getPasosPreparacion() {
        return pasosPreparacion;
    }

    public void setPasosPreparacion(List<String> pasosPreparacion) {
        this.pasosPreparacion = pasosPreparacion;
    }
}