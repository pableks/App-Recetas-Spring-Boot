package com.example.RecetaApp.service;

import java.util.List;
import java.util.Optional;

import com.example.RecetaApp.model.Receta;
import com.example.RecetaApp.model.Usuario;

public interface RecetaService {
    List<Receta> getAllRecetas();
    Optional<Receta> getRecetaById(Long id);
    Receta createReceta(Receta receta, Usuario usuario);
    Receta updateReceta(Long id, Receta receta);
    void deleteReceta(Long id);
    List<Receta> getRecetasByUsuario(Usuario usuario);
    List<Receta> getRecetasByDificultad(String dificultad);
    boolean isRecetaOwner(Long recetaId, Long userId);
}