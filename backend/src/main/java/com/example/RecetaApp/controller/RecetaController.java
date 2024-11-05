package com.example.RecetaApp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.example.RecetaApp.dto.ApiResponse;
import com.example.RecetaApp.dto.RecetaDTO;
import com.example.RecetaApp.model.Receta;
import com.example.RecetaApp.model.Usuario;
import com.example.RecetaApp.security.JwtUtils;
import com.example.RecetaApp.service.AuthService;
import com.example.RecetaApp.service.RecetaService;
import com.example.RecetaApp.service.UserSessionService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/recetas")
public class RecetaController {
    
    private static final Logger log = LoggerFactory.getLogger(RecetaController.class);
    private static final String JWT_COOKIE_NAME = "AUTH-TOKEN";
    private static final String SESSION_ID_COOKIE = "SESSION-ID";

    @Autowired
    private UserSessionService userSessionService;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RecetaService recetaService;

    @Autowired
    private AuthService authService;

    // Public endpoints remain unchanged
    @GetMapping
    public ResponseEntity<List<RecetaDTO>> getAllRecetas() {
        List<Receta> recetas = recetaService.getAllRecetas();
        List<RecetaDTO> recetasDTO = recetas.stream()
            .map(RecetaDTO::new)
            .collect(Collectors.toList());
        return ResponseEntity.ok(recetasDTO);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecetaDTO> getRecetaById(@PathVariable Long id) {
        return recetaService.getRecetaById(id)
            .map(receta -> ResponseEntity.ok(new RecetaDTO(receta)))
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/dificultad/{dificultad}")
    public ResponseEntity<List<Receta>> getRecetasByDificultad(@PathVariable String dificultad) {
        List<Receta> recetas = recetaService.getRecetasByDificultad(dificultad);
        return ResponseEntity.ok(recetas);
    }

    // Authenticated endpoints
    @GetMapping("/mis-recetas")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getMisRecetas(HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            log.debug("Current authentication: principal={}, authorities={}", 
                     authentication.getPrincipal(), 
                     authentication.getAuthorities());

            Usuario currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                log.debug("No current user found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Usuario no autenticado"));
            }

            log.debug("Current user roles: {}", currentUser.getRoles());

            if (!validateSession(request, currentUser.getUsername())) {
                log.debug("Invalid session for user: {}", currentUser.getUsername());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Sesión inválida"));
            }

            List<Receta> recetas = recetaService.getRecetasByUsuario(currentUser);
            return ResponseEntity.ok(new ApiResponse(true, 
                recetas.isEmpty() ? "No hay recetas" : "Recetas recuperadas exitosamente",
                recetas));

        } catch (Exception e) {
            log.error("Error al obtener las recetas del usuario: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Error al obtener las recetas"));
        }
    }

    @PostMapping("/crear")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> createReceta(
            @Valid @RequestBody Receta receta,
            HttpServletRequest request) {
        try {
            Usuario currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Usuario no autenticado"));
            }

            if (!validateSession(request, currentUser.getUsername())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Sesión inválida"));
            }

            receta.setCreadoPor(currentUser);
            Receta createdReceta = recetaService.createReceta(receta, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Receta creada exitosamente", createdReceta));
        } catch (Exception e) {
            log.error("Error al crear la receta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Error al crear la receta"));
        }
    }

    @PutMapping("/actualizar/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateReceta(
            @PathVariable Long id, 
            @Valid @RequestBody Receta receta,
            HttpServletRequest request) {
        try {
            Usuario currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Usuario no autenticado"));
            }

            if (!validateSession(request, currentUser.getUsername())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Sesión inválida"));
            }

            if (!authService.canModifyReceta(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "No tienes permiso para modificar esta receta"));
            }

            Optional<Receta> existingReceta = recetaService.getRecetaById(id);
            if (!existingReceta.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Receta no encontrada"));
            }

            receta.setCreadoPor(currentUser);
            Receta updatedReceta = recetaService.updateReceta(id, receta);
            return ResponseEntity.ok(new ApiResponse(true, "Receta actualizada exitosamente", updatedReceta));
        } catch (Exception e) {
            log.error("Error al actualizar la receta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Error al actualizar la receta"));
        }
    }

    @DeleteMapping("/eliminar/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteReceta(
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            Usuario currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Usuario no autenticado"));
            }

            if (!validateSession(request, currentUser.getUsername())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Sesión inválida"));
            }

            if (!authService.canModifyReceta(id)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "No tienes permiso para eliminar esta receta"));
            }

            Optional<Receta> receta = recetaService.getRecetaById(id);
            if (!receta.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Receta no encontrada"));
            }

            recetaService.deleteReceta(id);
            return ResponseEntity.ok(new ApiResponse(true, "Receta eliminada exitosamente"));
        } catch (Exception e) {
            log.error("Error al eliminar la receta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Error al eliminar la receta"));
        }
    }

    private boolean validateSession(HttpServletRequest request, String username) {
        try {
            Cookie[] cookies = request.getCookies();
            if (cookies == null) return false;

            String jwt = null;
            String sessionId = null;
            String userAgent = request.getHeader("User-Agent");

            for (Cookie cookie : cookies) {
                if (JWT_COOKIE_NAME.equals(cookie.getName())) {
                    jwt = cookie.getValue();
                } else if (SESSION_ID_COOKIE.equals(cookie.getName())) {
                    sessionId = cookie.getValue();
                }
            }

            if (jwt == null || sessionId == null) {
                log.debug("Missing required cookies");
                return false;
            }

            if (!jwtUtils.validateJwtToken(jwt)) {
                log.debug("Invalid JWT token");
                return false;
            }

            String tokenUsername = jwtUtils.getUserNameFromJwtToken(jwt);
            if (!username.equals(tokenUsername)) {
                log.debug("Username mismatch");
                return false;
            }

            return userSessionService.validateSession(sessionId, username, userAgent);

        } catch (Exception e) {
            log.error("Error validating session: {}", e.getMessage());
            return false;
        }
    }
}