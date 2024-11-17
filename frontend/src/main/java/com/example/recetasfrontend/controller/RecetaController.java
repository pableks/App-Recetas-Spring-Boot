package com.example.recetasfrontend.controller;

import com.example.recetasfrontend.model.Recipe;
import com.example.recetasfrontend.service.RecipeApiService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class RecetaController {
    private final RecipeApiService recipeApiService;
    private static final Logger log = LoggerFactory.getLogger(RecetaController.class);  // Add this line

    @Autowired
    public RecetaController(RecipeApiService recipeApiService) {
        this.recipeApiService = recipeApiService;
    }

    @GetMapping({"/", "/recetas"})
    public String viewRecetas(HttpServletRequest request, Model model) {
        try {
            List<Recipe> recipes = recipeApiService.getAllRecipes(request);
            model.addAttribute("recipes", recipes);
            
            addAuthAttributesToModel(request, model);
            
            if (request.getParameter("deleted") != null) {
                model.addAttribute("successMessage", "Recipe successfully deleted.");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Error loading recipes: " + e.getMessage());
        }
        return "recetas";
    }

    @GetMapping("/recetas/view/{id}")
    public String viewReceta(@PathVariable Long id, HttpServletRequest request, Model model) {
        try {
            Recipe recipe = recipeApiService.getRecipeById(id, request);
            model.addAttribute("recipe", recipe);
            addAuthAttributesToModel(request, model);
        } catch (Exception e) {
            model.addAttribute("error", "Error loading recipe: " + e.getMessage());
        }
        return "receta-detail";
    }

    @GetMapping("/recetas/create")
    public String createRecetaForm(HttpServletRequest request, Model model) {
        // Initialize a new Recipe with empty lists
        Recipe recipe = new Recipe();
        recipe.setListaIngredientes(new ArrayList<>());
        recipe.setPasosPreparacion(new ArrayList<>());
        
        model.addAttribute("recipe", recipe);
        addAuthAttributesToModel(request, model);  // Add this line

        return "receta-form";
    }

    @PostMapping("/recetas/create")
    public String createReceta(@ModelAttribute("recipe") @Valid Recipe recipe, 
                             BindingResult result,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "receta-form";
        }

        try {
            // Filter out empty values
            recipe.setListaIngredientes(recipe.getListaIngredientes().stream()
                .filter(i -> i != null && !i.trim().isEmpty())
                .collect(Collectors.toList()));
            
            recipe.setPasosPreparacion(recipe.getPasosPreparacion().stream()
                .filter(p -> p != null && !p.trim().isEmpty())
                .collect(Collectors.toList()));

            Recipe savedRecipe = recipeApiService.createRecipe(recipe, request);
            return "redirect:/recetas/view/" + savedRecipe.getId();
        } catch (Exception e) {
            log.error("Error creating recipe", e);
            redirectAttributes.addFlashAttribute("error", "Error al crear la receta: " + e.getMessage());
            return "redirect:/recetas/create";
        }
    }

    @GetMapping("/recetas/edit/{id}")
    public String editRecetaForm(@PathVariable Long id, HttpServletRequest request, Model model) {
        if (!isAuthenticated(request)) {
            return "redirect:/auth/login";
        }
        
        try {
            Recipe recipe = recipeApiService.getRecipeById(id, request);
            if (recipe == null) {
                throw new RuntimeException("Recipe not found");
            }
            
            // Ensure lists are initialized
            if (recipe.getListaIngredientes() == null) {
                recipe.setListaIngredientes(new ArrayList<>());
            }
            if (recipe.getPasosPreparacion() == null) {
                recipe.setPasosPreparacion(new ArrayList<>());
            }
            
            model.addAttribute("recipe", recipe);
            addAuthAttributesToModel(request, model);  // Add this line
            return "receta-edit";
        } catch (Exception e) {
            model.addAttribute("error", "Error loading recipe: " + e.getMessage());
            return "redirect:/recetas";
        }
    }

    @PostMapping("/recetas/edit/{id}")
    public String updateRecipe(@PathVariable Long id,
                            @ModelAttribute("recipe") @Valid Recipe recipe,
                            BindingResult result,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        if (!isAuthenticated(request)) {
            return "redirect:/auth/login";
        }

        if (result.hasErrors()) {
            addAuthAttributesToModel(request, model);
            return "receta-edit";
        }

        try {
            // Filter out empty values
            recipe.setListaIngredientes(recipe.getListaIngredientes().stream()
                .filter(i -> i != null && !i.trim().isEmpty())
                .collect(Collectors.toList()));
            
            recipe.setPasosPreparacion(recipe.getPasosPreparacion().stream()
                .filter(p -> p != null && !p.trim().isEmpty())
                .collect(Collectors.toList()));

            // Convert lists to strings
            recipe.setIngredientes(recipe.getIngredientsAsString());
            recipe.setPreparacion(recipe.getPreparacionAsString());

            recipe.setId(id); // Ensure the ID is set
            Recipe updatedRecipe = recipeApiService.updateRecipe(id, recipe, request);
            
            redirectAttributes.addFlashAttribute("successMessage", "Receta actualizada exitosamente");
            return "redirect:/recetas/view/" + updatedRecipe.getId();
        } catch (Exception e) {
            log.error("Error updating recipe", e);
            redirectAttributes.addFlashAttribute("error", "Error al actualizar la receta: " + e.getMessage());
            return "redirect:/recetas/edit/" + id;
        }
    }

    @GetMapping("/recetas/my-recipes")
    public String myRecipes(HttpServletRequest request, Model model) {
        if (!isAuthenticated(request)) {
            return "redirect:/auth/login";
        }
        
        try {
            List<Recipe> myRecipes = recipeApiService.getMyRecipes(request);
            model.addAttribute("recipes", myRecipes);
            
            // Add authentication attributes for navbar
            addAuthAttributesToModel(request, model);
            
            if (request.getParameter("deleted") != null) {
                model.addAttribute("successMessage", "Recipe successfully deleted.");
            }
            
            log.info("Retrieved {} recipes for user", 
                myRecipes != null ? myRecipes.size() : 0);
            
        } catch (Exception e) {
            log.error("Error loading user recipes", e);
            model.addAttribute("error", "Error loading your recipes: " + e.getMessage());
        }
        
        return "my-recipes";
    }

    // Add delete method to handle recipe deletion
    @PostMapping("/recetas/{id}")
    public String deleteRecipe(@PathVariable Long id, 
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        if (!isAuthenticated(request)) {
            return "redirect:/auth/login";
        }

        try {
            recipeApiService.deleteRecipe(id, request);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Recipe deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting recipe", e);
            redirectAttributes.addFlashAttribute("error", 
                "Error deleting recipe: " + e.getMessage());
        }

        return "redirect:/recetas/my-recipes";
    }


    private boolean isAuthenticated(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return false;
        }

        boolean hasAuthToken = false;
        boolean hasSessionId = false;

        for (Cookie cookie : request.getCookies()) {
            if ("AUTH-TOKEN".equals(cookie.getName()) && 
                cookie.getValue() != null && 
                !cookie.getValue().isEmpty()) {
                hasAuthToken = true;
            }
            if ("SESSION-ID".equals(cookie.getName()) && 
                cookie.getValue() != null && 
                !cookie.getValue().isEmpty()) {
                hasSessionId = true;
            }
        }

        return hasAuthToken || hasSessionId;
    }

    private void addAuthAttributesToModel(HttpServletRequest request, Model model) {
        boolean isAuthenticated = isAuthenticated(request);
        model.addAttribute("isAuthenticated", isAuthenticated);
        
        if (isAuthenticated && request.getCookies() != null) {
            String authToken = null;
            String sessionId = null;
            
            for (Cookie cookie : request.getCookies()) {
                if ("AUTH-TOKEN".equals(cookie.getName())) {
                    authToken = cookie.getValue();
                } else if ("SESSION-ID".equals(cookie.getName())) {
                    sessionId = cookie.getValue();
                }
            }
            
            if (authToken != null) {
                model.addAttribute("authToken", authToken);
            }
            if (sessionId != null) {
                model.addAttribute("sessionId", sessionId);
            }
        }
    }
}