package com.example.recetasfrontend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {
    private Long id;

    @NotBlank(message = "El nombre de la receta es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @NotNull(message = "El tiempo de preparación es obligatorio")
    @Min(value = 1, message = "El tiempo de preparación debe ser mayor a 0 minutos")
    private Integer tiempoPreparacion;

    @NotBlank(message = "La dificultad es obligatoria")
    private String dificultad;

    @NotBlank(message = "El tipo de cocina es obligatorio")
    private String tipoCocina;

    @NotBlank(message = "El país de origen es obligatorio")
    private String paisOrigen;

    @NotNull(message = "La porción es obligatoria")
    @Min(value = 1, message = "La porción debe ser al menos para 1 persona")
    private Integer porcion;


    // Fields for backend string format
    private String ingredientes;
    private String preparacion;
    private String notasExtra;
    private List<?> links;

    @NotNull(message = "La lista de ingredientes no puede ser nula")
    @Size(min = 1, message = "Debe incluir al menos un ingrediente")
    private List<String> listaIngredientes;

    @NotNull(message = "Los pasos de preparación no pueden ser nulos")
    @Size(min = 1, message = "Debe incluir al menos un paso de preparación")
    private List<String> pasosPreparacion;

    private String creadorUsername;
    private String imageUrl;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // Helper methods for string/list conversion
    @JsonIgnore
    public String getIngredientsAsString() {
        if (listaIngredientes != null && !listaIngredientes.isEmpty()) {
            return String.join("\n", listaIngredientes);
        }
        return ingredientes != null ? ingredientes : "";
    }

    @JsonIgnore
    public String getPreparacionAsString() {
        if (pasosPreparacion != null && !pasosPreparacion.isEmpty()) {
            return String.join("\n", pasosPreparacion);
        }
        return preparacion != null ? preparacion : "";
    }

    @JsonIgnore
    public void setIngredientsFromString(String ingredients) {
        this.ingredientes = ingredients;
        if (ingredients != null && !ingredients.isEmpty()) {
            this.listaIngredientes = new ArrayList<>(Arrays.asList(ingredients.split("\n")));
        }
    }

    @JsonIgnore
    public void setPreparacionFromString(String prep) {
        this.preparacion = prep;
        if (prep != null && !prep.isEmpty()) {
            this.pasosPreparacion = new ArrayList<>(Arrays.asList(prep.split("\n")));
        }
    }

    // Custom method to add an ingredient
    public void addIngrediente(String ingrediente) {
        if (this.listaIngredientes == null) {
            this.listaIngredientes = new ArrayList<>();
        }
        this.listaIngredientes.add(ingrediente);
        this.ingredientes = getIngredientsAsString(); // Keep string format in sync
    }

    // Custom method to add a preparation step
    public void addPasoPreparacion(String paso) {
        if (this.pasosPreparacion == null) {
            this.pasosPreparacion = new ArrayList<>();
        }
        this.pasosPreparacion.add(paso);
        this.preparacion = getPreparacionAsString(); // Keep string format in sync
    }

    // Custom method to check if the recipe is owned by a specific user
    public boolean isOwnedBy(String username) {
        return creadorUsername != null && creadorUsername.equals(username);
    }

    // Enum for difficulty levels
    public enum Dificultad {
        BAJA("Baja"),
        MEDIA("Media"),
        ALTA("Alta");

        private final String displayValue;

        Dificultad(String displayValue) {
            this.displayValue = displayValue;
        }

        public String getDisplayValue() {
            return displayValue;
        }
    }

    // Custom validation method
    
    public boolean isValid() {
        return nombre != null && !nombre.trim().isEmpty() &&
               tiempoPreparacion != null && tiempoPreparacion > 0 &&
               dificultad != null &&
               tipoCocina != null && !tipoCocina.trim().isEmpty() &&
               paisOrigen != null && !paisOrigen.trim().isEmpty() &&
               porcion != null && porcion > 0 &&
               ((listaIngredientes != null && !listaIngredientes.isEmpty()) ||
                (ingredientes != null && !ingredientes.trim().isEmpty())) &&
               ((pasosPreparacion != null && !pasosPreparacion.isEmpty()) ||
                (preparacion != null && !preparacion.trim().isEmpty()));
    }

    // Method to create a copy of the recipe
    public Recipe copy() {
        Recipe copy = Recipe.builder()
                .id(this.id)
                .nombre(this.nombre)
                .tiempoPreparacion(this.tiempoPreparacion)
                .dificultad(this.dificultad)
                .ingredientes(this.ingredientes)
                .preparacion(this.preparacion)
                .tipoCocina(this.tipoCocina)
                .paisOrigen(this.paisOrigen)
                .porcion(this.porcion)
                .notasExtra(this.notasExtra)
                .creadorUsername(this.creadorUsername)
                .imageUrl(this.imageUrl)
                .fechaCreacion(this.fechaCreacion)
                .fechaActualizacion(this.fechaActualizacion)
                .build();

        if (this.listaIngredientes != null) {
            copy.setListaIngredientes(new ArrayList<>(this.listaIngredientes));
        }
        if (this.pasosPreparacion != null) {
            copy.setPasosPreparacion(new ArrayList<>(this.pasosPreparacion));
        }

        return copy;
    }
}