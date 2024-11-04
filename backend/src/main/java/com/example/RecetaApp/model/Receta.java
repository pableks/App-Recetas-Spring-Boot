package com.example.RecetaApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

@Entity
@Table(name = "recetas")
public class Receta extends RepresentationModel<Receta> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    @JsonBackReference
    private Usuario creadoPor;

    @NotNull(message = "El tiempo de preparación no puede estar vacío")
    @Min(value = 1, message = "El tiempo de preparación debe ser mayor a 0")
    @Column(name = "tiempo_preparacion")
    private Integer tiempoPreparacion;

    @NotBlank(message = "La dificultad no puede estar vacía")
    @Column(name = "dificultad")
    private String dificultad;

    @Column(name = "ingredientes", length = 4000)
    private String ingredientes;

    @ElementCollection
    @CollectionTable(
        name = "receta_lista_ingredientes",
        joinColumns = @JoinColumn(name = "receta_id")
    )
    @Column(name = "ingrediente")
    private List<String> listaIngredientes;

    @Column(name = "preparacion", length = 4000)
    private String preparacion;

    @ElementCollection
    @CollectionTable(
        name = "receta_pasos_preparacion",
        joinColumns = @JoinColumn(name = "receta_id")
    )
    @Column(name = "paso")
    private List<String> pasosPreparacion;

    // Getters y Setters
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

    public Usuario getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(Usuario creadoPor) {
        this.creadoPor = creadoPor;
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
