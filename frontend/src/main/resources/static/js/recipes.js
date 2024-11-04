function loadRecipes() {
    console.log('Loading recipes...');
    $("#loading-spinner").show();
    $("#error-container").hide();
    $("#recipes-container").hide();
    
    $.ajax({
        url: 'http://localhost:8080/api/recetas',
        method: 'GET',
        crossDomain: true,
        xhrFields: {
            withCredentials: true
        },
        beforeSend: function(xhr) {
            // The AUTH-TOKEN cookie will be automatically included
            // due to withCredentials: true
        },
        success: function(recipes) {
            console.log('Recipes loaded:', recipes);
            $("#loading-spinner").hide();
            $("#recipes-container").show();
            displayRecipes(recipes);
        },
        error: function(xhr, status, error) {
            console.error('Error loading recipes:', error);
            console.error('Status:', status);
            console.error('Response:', xhr.responseText);
            
            $("#loading-spinner").hide();
            $("#error-container")
                .show()
                .text('Error loading recipes. Please try again later. ' + error);
        }
    });
}

function displayRecipes(recipes) {
    const container = $("#recipes-container");
    container.empty();
    
    console.log('Displaying recipes:', recipes);
    
    if (!recipes || recipes.length === 0) {
        container.html(`
            <div class="col-12">
                <div class="alert alert-info">
                    No recipes found.
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

        const isOwner = recipe.creadorUsername === getCookie('SESSION-ID');

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
                            <a href="/recetas/view/${recipe.id}" class="btn btn-primary">Ver Receta</a>
                            ${isOwner ? `
                                <a href="/recetas/edit/${recipe.id}" class="btn btn-secondary">Editar</a>
                                <button onclick="deleteRecipe(${recipe.id})" class="btn btn-danger">Eliminar</button>
                            ` : ''}
                        </div>
                    </div>
                </div>
            </div>
        `;
        container.append(recipeCard);
    });
}

function deleteRecipe(id) {
    if (!confirm('¿Estás seguro de que quieres eliminar esta receta?')) {
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
            loadRecipes();
        },
        error: function(xhr, status, error) {
            alert('Error al eliminar la receta. Por favor intenta de nuevo.');
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