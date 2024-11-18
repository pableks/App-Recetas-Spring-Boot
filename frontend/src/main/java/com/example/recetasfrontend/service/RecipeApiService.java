package com.example.recetasfrontend.service;

import com.example.recetasfrontend.model.Recipe;
import com.example.recetasfrontend.model.ApiResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecipeApiService {
    private static final Logger logger = LoggerFactory.getLogger(RecipeApiService.class);
    private final RestTemplate restTemplate;
    private final String API_BASE_URL = "http://localhost:8080/api";

    @Autowired
    public RecipeApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private HttpHeaders createHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            String authToken = null;
            String sessionId = null;
            
            for (Cookie cookie : cookies) {
                if ("AUTH-TOKEN".equals(cookie.getName())) {
                    authToken = cookie.getValue();
                } else if ("SESSION-ID".equals(cookie.getName())) {
                    sessionId = cookie.getValue();
                }
            }

            if (authToken != null && sessionId != null) {
                String cookieValue = String.format("AUTH-TOKEN=%s; SESSION-ID=%s", authToken, sessionId);
                headers.add("Cookie", cookieValue);
                logger.debug("Added cookies to header: {}", cookieValue);
            } else {
                logger.warn("Missing required cookies - authToken: {}, sessionId: {}", 
                    authToken != null ? "present" : "missing",
                    sessionId != null ? "present" : "missing");
            }
        } else {
            logger.warn("No cookies found in request");
        }
        
        return headers;
    }

    public List<Recipe> getMyRecipes(HttpServletRequest request) {
        try {
            HttpHeaders headers = createHeaders(request);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            logger.debug("Fetching my recipes from: {}/recetas/mis-recetas", API_BASE_URL);
            logger.debug("Request headers: {}", headers);
            
            ResponseEntity<ApiResponse<List<Recipe>>> response = restTemplate.exchange(
                API_BASE_URL + "/recetas/mis-recetas",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ApiResponse<List<Recipe>>>() {}
            );
            
            ApiResponse<List<Recipe>> apiResponse = response.getBody();
            if (apiResponse != null && apiResponse.isSuccess()) {
                List<Recipe> recipes = apiResponse.getData();
                logger.debug("Received {} recipes", recipes != null ? recipes.size() : 0);
                return recipes;
            } else {
                String errorMessage = apiResponse != null ? apiResponse.getMessage() : "No response from server";
                throw new RuntimeException("Error fetching recipes: " + errorMessage);
            }
        } catch (Exception e) {
            logger.error("Error fetching my recipes", e);
            throw new RuntimeException("Error fetching recipes: " + e.getMessage(), e);
        }
    }

    public List<Recipe> getAllRecipes(HttpServletRequest request) {
        try {
            HttpHeaders headers = createHeaders(request);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            String url = API_BASE_URL + "/recetas";
            logger.debug("Fetching all recipes from: {}", url);
            logger.debug("Request headers: {}", headers);
            
            ResponseEntity<List<Recipe>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<Recipe>>() {}
            );
            
            List<Recipe> recipes = response.getBody();
            logger.debug("Received {} recipes", recipes != null ? recipes.size() : 0);
            return recipes;
        } catch (Exception e) {
            logger.error("Error fetching all recipes", e);
            throw new RuntimeException("Error fetching recipes: " + e.getMessage(), e);
        }
    }

    public Recipe getRecipeById(Long id, HttpServletRequest request) {
        try {
            HttpHeaders headers = createHeaders(request);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            String url = API_BASE_URL + "/recetas/" + id;
            logger.debug("Fetching recipe with id: {}", id);
            
            ResponseEntity<Recipe> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Recipe.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            logger.error("Error fetching recipe with id: " + id, e);
            throw new RuntimeException("Error fetching recipe: " + e.getMessage(), e);
        }
    }

    public Recipe createRecipe(Recipe recipe, HttpServletRequest request) {
        try {
            HttpHeaders headers = createHeaders(request);
            HttpEntity<Recipe> entity = new HttpEntity<>(recipe, headers);
            
            logger.debug("Creating new recipe: {}", recipe.getNombre());
            
            ResponseEntity<ApiResponse<Recipe>> response = restTemplate.exchange(
                API_BASE_URL + "/recetas/crear",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<ApiResponse<Recipe>>() {}
            );
            
            ApiResponse<Recipe> apiResponse = response.getBody();
            if (apiResponse != null && apiResponse.isSuccess()) {
                return apiResponse.getData();
            } else {
                String errorMessage = apiResponse != null ? apiResponse.getMessage() : "No response from server";
                throw new RuntimeException("Error creating recipe: " + errorMessage);
            }
        } catch (Exception e) {
            logger.error("Error creating recipe", e);
            throw new RuntimeException("Error creating recipe: " + e.getMessage(), e);
        }
    }

    public Recipe updateRecipe(Long id, Recipe recipe, HttpServletRequest request) {
    try {
        HttpHeaders headers = createHeaders(request);
        
        // Convert lists to string format as expected by backend
        String ingredientes = recipe.getListaIngredientes() != null ? 
            String.join("\n", recipe.getListaIngredientes()) : "";
        String preparacion = recipe.getPasosPreparacion() != null ?
            String.join("\n", recipe.getPasosPreparacion()) : "";
        
        // Create a map of data to match backend expectations
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("id", id);
        requestBody.put("nombre", recipe.getNombre());
        requestBody.put("tiempoPreparacion", recipe.getTiempoPreparacion());
        requestBody.put("dificultad", recipe.getDificultad());
        requestBody.put("ingredientes", ingredientes);
        requestBody.put("preparacion", preparacion);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        logger.debug("Updating recipe with id: {}", id);
        logger.debug("Request body: {}", requestBody);
        
        ResponseEntity<ApiResponse<Recipe>> response = restTemplate.exchange(
            API_BASE_URL + "/recetas/actualizar/" + id,
            HttpMethod.PUT,
            entity,
            new ParameterizedTypeReference<ApiResponse<Recipe>>() {}
        );
        
        ApiResponse<Recipe> apiResponse = response.getBody();
        if (apiResponse != null && apiResponse.isSuccess()) {
            Recipe updatedRecipe = apiResponse.getData();
            
            // Convert backend response back to frontend format if needed
            if (updatedRecipe != null) {
                if (updatedRecipe.getIngredientes() != null) {
                    updatedRecipe.setListaIngredientes(
                        Arrays.asList(updatedRecipe.getIngredientes().split("\n"))
                    );
                }
                if (updatedRecipe.getPreparacion() != null) {
                    updatedRecipe.setPasosPreparacion(
                        Arrays.asList(updatedRecipe.getPreparacion().split("\n"))
                    );
                }
            }
            
            return updatedRecipe;
        } else {
            String errorMessage = apiResponse != null ? apiResponse.getMessage() : "No response from server";
            throw new RuntimeException("Error updating recipe: " + errorMessage);
        }
    } catch (Exception e) {
        logger.error("Error updating recipe with id: " + id, e);
        throw new RuntimeException("Error updating recipe: " + e.getMessage(), e);
    }
}

    public void deleteRecipe(Long id, HttpServletRequest request) {
        try {
            HttpHeaders headers = createHeaders(request);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            logger.debug("Deleting recipe with id: {}", id);
            
            ResponseEntity<ApiResponse<Void>> response = restTemplate.exchange(
                API_BASE_URL + "/recetas/eliminar/" + id,
                HttpMethod.DELETE,
                entity,
                new ParameterizedTypeReference<ApiResponse<Void>>() {}
            );
            
            ApiResponse<Void> apiResponse = response.getBody();
            if (apiResponse == null || !apiResponse.isSuccess()) {
                String errorMessage = apiResponse != null ? apiResponse.getMessage() : "No response from server";
                throw new RuntimeException("Error deleting recipe: " + errorMessage);
            }
        } catch (Exception e) {
            logger.error("Error deleting recipe with id: " + id, e);
            throw new RuntimeException("Error deleting recipe: " + e.getMessage(), e);
        }
    }
}