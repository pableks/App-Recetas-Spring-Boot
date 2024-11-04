package com.example.RecetaApp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.RecetaApp.model.Receta;
import com.example.RecetaApp.model.Usuario;
import com.example.RecetaApp.repository.RecetaRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RecetaServiceImpl implements RecetaService {

    @Autowired
    private RecetaRepository recetaRepository;

    @Override
    public List<Receta> getAllRecetas() {
        return recetaRepository.findAll();
    }

    @Override
    public Optional<Receta> getRecetaById(Long id) {
        return recetaRepository.findById(id);
    }

    @Override
    public Receta createReceta(Receta receta, Usuario usuario) {
        receta.setCreadoPor(usuario);
        
        // Procesar ingredientes
        if (receta.getIngredientes() != null && !receta.getIngredientes().isEmpty()) {
            List<String> ingredientes = Arrays.asList(receta.getIngredientes().split("\n"));
            receta.setListaIngredientes(ingredientes);
        }
        
        // Procesar pasos de preparación
        if (receta.getPreparacion() != null && !receta.getPreparacion().isEmpty()) {
            List<String> pasos = Arrays.asList(receta.getPreparacion().split("\n"));
            receta.setPasosPreparacion(pasos);
        }
        
        return recetaRepository.save(receta);
    }

    @Override
public Receta updateReceta(Long id, Receta receta) {
    try {
        return recetaRepository.findById(id)
            .map(existingReceta -> {
                // Mantener la referencia al usuario creador
                Usuario creadoPor = existingReceta.getCreadoPor();
                
                // Actualizar campos básicos
                existingReceta.setNombre(receta.getNombre());
                existingReceta.setTiempoPreparacion(receta.getTiempoPreparacion());
                existingReceta.setDificultad(receta.getDificultad());
                existingReceta.setIngredientes(receta.getIngredientes());
                existingReceta.setPreparacion(receta.getPreparacion());
                
                // Procesar ingredientes si existen
                if (receta.getIngredientes() != null && !receta.getIngredientes().isEmpty()) {
                    List<String> ingredientes = Arrays.asList(receta.getIngredientes().split("\n"));
                    existingReceta.setListaIngredientes(new ArrayList<>(ingredientes));
                }
                
                // Procesar pasos si existen
                if (receta.getPreparacion() != null && !receta.getPreparacion().isEmpty()) {
                    List<String> pasos = Arrays.asList(receta.getPreparacion().split("\n"));
                    existingReceta.setPasosPreparacion(new ArrayList<>(pasos));
                }
                
                // Mantener el usuario creador
                existingReceta.setCreadoPor(creadoPor);
                
                return recetaRepository.save(existingReceta);
            })
            .orElse(null);

        } catch (Exception e) {
            Logger log = LoggerFactory.getLogger(RecetaServiceImpl.class);
            log.error("Error actualizando receta: {}", e.getMessage(), e);
            throw new RuntimeException("Error actualizando receta: " + e.getMessage());
        }
}

    @Override
    public void deleteReceta(Long id) {
        recetaRepository.deleteById(id);
    }

    @Override
    public List<Receta> getRecetasByUsuario(Usuario usuario) {
        return recetaRepository.findByCreadoPor(usuario);
    }

    @Override
    public List<Receta> getRecetasByDificultad(String dificultad) {
        return recetaRepository.findByDificultad(dificultad);
    }

    @Override
    public boolean isRecetaOwner(Long recetaId, Long userId) {
        return recetaRepository.findById(recetaId)
            .map(receta -> receta.getCreadoPor().getId().equals(userId))
            .orElse(false);
    }
}