package com.example.RecetaApp.service;

import java.util.List;
import java.util.Optional;

import com.example.RecetaApp.model.Producto;

public interface ProductoService {
    List<Producto> getAllProductos();
    Optional<Producto> getProductoById(Long id);
    Producto createProducto(Producto producto);
    Producto updateProducto(Long id, Producto producto);
    void deleteProducto(Long id);
    List<Producto> getProductosByCategoria(String categoria);
    Producto getProductoBySku(String sku);
}