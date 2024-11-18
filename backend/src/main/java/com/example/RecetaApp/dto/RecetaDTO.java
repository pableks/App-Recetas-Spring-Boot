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
    private String tipoCocina;
    private String paisOrigen;
    private Integer porcion;
    private String notasExtra;

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
        this.tipoCocina = receta.getTipoCocina();
        this.paisOrigen = receta.getPaisOrigen();
        this.porcion = receta.getPorcion();
        this.notasExtra = receta.getNotasExtra();
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
    // Añadir getters y setters para los nuevos campos
    public String getTipoCocina() {
        return tipoCocina;
    }

    public void setTipoCocina(String tipoCocina) {
        this.tipoCocina = tipoCocina;
    }

    public String getPaisOrigen() {
        return paisOrigen;
    }

    public void setPaisOrigen(String paisOrigen) {
        this.paisOrigen = paisOrigen;
    }

    public Integer getPorcion() {
        return porcion;
    }

    public void setPorcion(Integer porcion) {
        this.porcion = porcion;
    }

    public String getNotasExtra() {
        return notasExtra;
    }

    public void setNotasExtra(String notasExtra) {
        this.notasExtra = notasExtra;
    }
}