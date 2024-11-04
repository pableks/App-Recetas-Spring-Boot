package com.example.RecetaApp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.RecetaApp.model.Producto;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByCategoria(String categoria);
    Producto findBySku(String sku);
}