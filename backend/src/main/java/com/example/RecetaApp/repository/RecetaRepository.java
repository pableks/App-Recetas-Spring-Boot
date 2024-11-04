package com.example.RecetaApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RecetaApp.model.Receta;
import com.example.RecetaApp.model.Usuario;

import java.util.List;

public interface RecetaRepository extends JpaRepository<Receta, Long> {
    List<Receta> findByCreadoPor(Usuario usuario);
    List<Receta> findByDificultad(String dificultad);
}