    package com.example.RecetaApp.service;

    import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.security.core.Authentication;
    import org.springframework.security.core.context.SecurityContextHolder;
    import org.springframework.stereotype.Service;

import com.example.RecetaApp.model.Receta;
import com.example.RecetaApp.model.Role;
import com.example.RecetaApp.model.Usuario;
import com.example.RecetaApp.repository.RecetaRepository;
import com.example.RecetaApp.repository.UsuariosRepository;

    @Service
    public class AuthService {
        
        @Autowired
        private UsuariosRepository usuariosRepository;

        @Autowired
        private RecetaRepository recetaRepository;

        public Usuario getCurrentUser() {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (!isCurrentUserAuthenticated()) {
                    return null;
                }
                return usuariosRepository.findByUsername(authentication.getName());
            } catch (Exception e) {
                return null;
            }
        }

        public boolean isCurrentUserAuthenticated() {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                return authentication != null && 
                    authentication.isAuthenticated() && 
                    !authentication.getPrincipal().equals("anonymousUser") &&
                    authentication.getName() != null &&
                    usuariosRepository.findByUsername(authentication.getName()) != null;
            } catch (Exception e) {
                return false;
            }
        }

        public boolean isCurrentUser(Long userId) {
            Usuario currentUser = getCurrentUser();
            return currentUser != null && currentUser.getId().equals(userId);
        }

        public boolean canModifyReceta(Long recetaId) {
            try {
                Usuario currentUser = getCurrentUser();
                if (currentUser == null) {
                    return false;
                }
    
                // Si el usuario es admin, tiene permiso
                if (currentUser.getRoles().contains(Role.ADMIN)) {
                    return true;
                }
    
                // Buscar la receta
                return recetaRepository.findById(recetaId)
                    .map(receta -> {
                        Usuario creadoPor = receta.getCreadoPor();
                        return creadoPor != null && creadoPor.getId().equals(currentUser.getId());
                    })
                    .orElse(false);
        } catch (Exception e) {
            return false;
        }
    }
    }