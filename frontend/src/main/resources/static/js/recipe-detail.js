function loadRecipeDetail() {
    // Get recipe ID from the URL
    const path = window.location.pathname;
    const recipeId = path.split('/').pop();
    
    $("#loading-spinner").show();
    $("#error-container").hide();
    $("#recipe-detail").hide();
    
    $.ajax({
        url: `http://localhost:8080/api/recetas/${recipeId}`,
        method: 'GET',
        crossDomain: true,
        xhrFields: {
            withCredentials: true
        },
        success: function(recipe) {
            console.log('Recipe loaded:', recipe);
            displayRecipeDetail(recipe);
            $("#loading-spinner").hide();
            $("#recipe-detail").show();
        },
        error: function(xhr, status, error) {
            console.error('Error loading recipe:', error);
            $("#loading-spinner").hide();
            $("#error-container")
                .show()
                .text('Error loading recipe details. Please try again later. ' + error);
        }
    });
}

function displayRecipeDetail(recipe) {
    // Set title
    $("#recipe-title").text(recipe.nombre);
    
    // Set badges
    $("#prep-time").html(`⏱️ ${recipe.tiempoPreparacion} min`);
    $("#difficulty").html(`📊 ${recipe.dificultad.toLowerCase()}`);
    
    // Clear and populate ingredients
    const ingredientsList = $("#ingredients-list");
    ingredientsList.empty();
    if (recipe.listaIngredientes && recipe.listaIngredientes.length > 0) {
        recipe.listaIngredientes.forEach(ingredient => {
            ingredientsList.append(`<li class="list-group-item border-0">${ingredient}</li>`);
        });
    } else {
        ingredientsList.append('<li class="list-group-item border-0">No ingredients listed</li>');
    }
    
    // Clear and populate instructions
    const instructionsList = $("#instructions-list");
    instructionsList.empty();
    if (recipe.pasosPreparacion && recipe.pasosPreparacion.length > 0) {
        recipe.pasosPreparacion.forEach((step, index) => {
            const stepText = step.replace(/^\d+\.\s*/, '');
            instructionsList.append(`<li class="list-group-item border-0">${stepText}</li>`);
        });
    } else {
        instructionsList.append('<li class="list-group-item border-0">No instructions available</li>');
    }
    
    // Handle edit/delete buttons
    const actionButtons = $("#action-buttons");
    actionButtons.empty();
    
    const currentUser = getCookie('SESSION-ID');
    console.log('Current user:', currentUser);
    console.log('Recipe creator:', recipe.creadorUsername);
    
    // Check if user is owner
    if (currentUser && recipe.creadorUsername === currentUser) {
        actionButtons.html(`
            <a href="/recetas/edit/${recipe.id}" class="btn btn-secondary">
                <i class="bi bi-pencil"></i> Edit Recipe
            </a>
            <button onclick="deleteRecipe(${recipe.id})" class="btn btn-danger">
                <i class="bi bi-trash"></i> Delete Recipe
            </button>
        `);
    }
}

function deleteRecipe(id) {
    if (!confirm('Are you sure you want to delete this recipe?')) {
        return;
    }

    $.ajax({
        url: `http://localhost:8080/api/recetas/${id}`,
        method: 'DELETE',
        crossDomain: true,
        xhrFields: {
            withCredentials: true
        },
        success: function() {
            window.location.href = '/recetas?deleted=true';
        },
        error: function(xhr, status, error) {
            alert('Error deleting recipe. Please try again.');
            console.error('Error:', error);
        }
    });
}

function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
    return null;
}

// Load recipe details when the page loads
$(document).ready(function() {
    loadRecipeDetail();
});