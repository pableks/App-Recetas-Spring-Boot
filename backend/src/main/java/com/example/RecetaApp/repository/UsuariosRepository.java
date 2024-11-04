package com.example.RecetaApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RecetaApp.model.Usuario;

public interface UsuariosRepository extends JpaRepository<Usuario, Long>{
    Usuario findByUsername(String username);
}
