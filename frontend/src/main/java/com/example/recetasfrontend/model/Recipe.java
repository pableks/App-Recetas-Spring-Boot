package com.example.recetasfrontend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Data                   // Generates getters, setters, toString, equals, and hashCode
@Builder               // Enables builder pattern for object creation
@NoArgsConstructor     // Generates no-args constructor
@AllArgsConstructor    // Generates constructor with all args
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

    private String ingredientes;  // Add this field as it's in the API response
    private String preparacion;   // Add this field as it's in the API response
    private List<?> links;        // Add this field as it's in the API response


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

    // Custom method to add an ingredient
    public void addIngrediente(String ingrediente) {
        this.listaIngredientes.add(ingrediente);
    }

    // Custom method to add a preparation step
    public void addPasoPreparacion(String paso) {
        this.pasosPreparacion.add(paso);
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
               listaIngredientes != null && !listaIngredientes.isEmpty() &&
               pasosPreparacion != null && !pasosPreparacion.isEmpty();
    }

    // Method to create a copy of the recipe
    public Recipe copy() {
        return Recipe.builder()
                .id(this.id)
                .nombre(this.nombre)
                .tiempoPreparacion(this.tiempoPreparacion)
                .dificultad(this.dificultad)
                .listaIngredientes(List.copyOf(this.listaIngredientes))
                .pasosPreparacion(List.copyOf(this.pasosPreparacion))
                .creadorUsername(this.creadorUsername)
                .imageUrl(this.imageUrl)
                .fechaCreacion(this.fechaCreacion)
                .fechaActualizacion(this.fechaActualizacion)
                .build();
    }
}