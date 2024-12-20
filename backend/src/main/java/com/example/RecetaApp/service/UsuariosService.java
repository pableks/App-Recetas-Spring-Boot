package com.example.RecetaApp.service;

import java.util.List;
import java.util.Optional;

import com.example.RecetaApp.model.Despacho;
import com.example.RecetaApp.model.Role;
import com.example.RecetaApp.model.Usuario;

public interface UsuariosService {
    List<Usuario> getAllUsuarios();
    Optional<Usuario> getUsuarioById(Long id);
    Usuario createUsuario(Usuario usuario);
    Usuario updateUsuario(Long id, Usuario usuario);
    void deleteUsuario(Long id);
    Usuario findByUsername(String username);
    void removeRoleFromUser(Long userId, Role role);
    void removeDespachoFromUser(Long userId, Long despachoId);
    Usuario addRoleToUser(Long userId, Role role);
    Usuario addDespachoToUser(Long userId, Despacho despacho);
    boolean isCurrentUser(Long userId);
}