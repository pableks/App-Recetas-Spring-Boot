function loadMyRecipes() {
    console.log('Loading my recipes...');
    $("#loading-spinner").show();
    $("#error-container").hide();
    $("#recipes-container").hide();
    
    $.ajax({
        url: 'http://localhost:8080/api/recetas/mis-recetas',
        method: 'GET',
        crossDomain: true,
        xhrFields: {
            withCredentials: true
        },
        success: function(response) {
            console.log('API Response:', response);
            
            // Handle different response formats
            let recipes = [];
            if (response.data) {
                recipes = response.data;
            } else if (Array.isArray(response)) {
                recipes = response;
            } else if (typeof response === 'object') {
                recipes = [response];
            }
            
            if (!Array.isArray(recipes)) {
                console.error('Invalid response format:', response);
                $("#loading-spinner").hide();
                $("#error-container")
                    .show()
                    .text('Error: Unexpected response format from server');
                return;
            }
            
            console.log('Processed recipes:', recipes);
            $("#loading-spinner").hide();
            $("#recipes-container").show();
            displayMyRecipes(recipes);  // Use new display function specifically for my recipes
        },
        error: function(xhr, status, error) {
            console.error('Error loading my recipes:', error);
            console.error('Status:', status);
            console.error('Response:', xhr.responseText);
            
            if (xhr.status === 401) {
                window.location.href = '/auth/login';
                return;
            }
            
            $("#loading-spinner").hide();
            $("#error-container")
                .show()
                .text('Error loading your recipes. Please try again later. ' + error);
        }
    });
}

function displayMyRecipes(recipes) {
    const container = $("#recipes-container");
    container.empty();
    
    console.log('Displaying my recipes:', recipes);
    
    if (!recipes || recipes.length === 0) {
        container.html(`
            <div class="col-12">
                <div class="alert alert-info">
                    No recipes found. <a href="/recetas/create" class="alert-link">Create your first recipe</a>
                </div>
            </div>
        `);
        return;
    }
    
    recipes.forEach(recipe => {
        console.log('Processing recipe:', recipe);
        const ingredients = recipe.listaIngredientes && recipe.listaIngredientes.length > 0 
            ? `<p class="card-text small text-muted mb-0">
                ${recipe.listaIngredientes.slice(0, 3).join('・')}
                ${recipe.listaIngredientes.length > 3 ? '...' : ''}
               </p>`
            : '<p class="text-muted mb-0">No ingredients listed</p>';

        const recipeCard = `
            <div class="col">
                <div class="card h-100 shadow-sm">
                    ${recipe.imageUrl ? 
                        `<img src="${recipe.imageUrl}" class="card-img-top" alt="${recipe.nombre}">` : 
                        '<div class="card-img-top bg-light text-center py-5">No Image</div>'
                    }
                    <div class="card-body">
                        <h5 class="card-title">${recipe.nombre}</h5>
                        <div class="d-flex align-items-center mb-2">
                            <span class="badge bg-light text-dark me-2">⏱️ ${recipe.tiempoPreparacion} min</span>
                            <span class="badge bg-light text-dark">📊 ${recipe.dificultad.toLowerCase()}</span>
                        </div>
                        ${ingredients}
                    </div>
                    <div class="card-footer bg-transparent">
                        <div class="d-grid gap-2">
                            <a href="/recetas/view/${recipe.id}" class="btn btn-primary">View Recipe</a>
                            <a href="/recetas/edit/${recipe.id}" class="btn btn-secondary">Edit</a>
                            <button onclick="deleteRecipe(${recipe.id})" class="btn btn-danger">Delete</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        container.append(recipeCard);
    });
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
            loadMyRecipes();  // Reload the recipes after deletion
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

// Call loadMyRecipes when the page loads
$(document).ready(function() {
    loadMyRecipes();
});