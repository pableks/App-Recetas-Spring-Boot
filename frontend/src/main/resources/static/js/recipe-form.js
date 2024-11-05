let recipeId = null;

$(document).ready(function() {
    // Check if we're editing an existing recipe
    const path = window.location.pathname;
    if (path.includes('/edit/')) {
        recipeId = path.split('/').pop();
        $('#form-title').text('Edit Recipe');
        loadRecipe(recipeId);
    } else {
        // Add initial empty ingredient and instruction fields for new recipe
        addIngredient();
        addInstruction();
    }
});

function loadRecipe(id) {
    $("#loading-spinner").show();
    $("#recipe-form").hide();
    $("#error-container").hide();

    $.ajax({
        url: `http://localhost:8080/api/recetas/${id}`,
        method: 'GET',
        crossDomain: true,
        xhrFields: {
            withCredentials: true
        },
        success: function(recipe) {
            populateForm(recipe);
            $("#loading-spinner").hide();
            $("#recipe-form").show();
        },
        error: function(xhr, status, error) {
            $("#loading-spinner").hide();
            $("#error-container")
                .show()
                .text('Error loading recipe. Please try again later. ' + error);
        }
    });
}

function populateForm(recipe) {
    $('#nombre').val(recipe.nombre);
    $('#tiempoPreparacion').val(recipe.tiempoPreparacion);
    $('#dificultad').val(recipe.dificultad);

    // Clear and populate ingredients
    $('#ingredients-container').empty();
    recipe.listaIngredientes.forEach(ingredient => {
        addIngredient(ingredient);
    });

    // Clear and populate instructions
    $('#instructions-container').empty();
    recipe.pasosPreparacion.forEach(instruction => {
        addInstruction(instruction.replace(/^\d+\.\s*/, ''));
    });
}

function addIngredient(value = '') {
    const newIngredient = `
        <div class="input-group mb-2">
            <input type="text" class="form-control ingredient-input" placeholder="Enter ingredient" value="${value}">
            <button type="button" class="btn btn-outline-danger" onclick="removeIngredient(this)">
                <i class="bi bi-trash"></i>
            </button>
        </div>
    `;
    $('#ingredients-container').append(newIngredient);
}

function removeIngredient(button) {
    if ($('.ingredient-input').length > 1) {
        $(button).closest('.input-group').remove();
    }
}

function addInstruction(value = '') {
    const index = $('.instruction-input').length + 1;
    const newInstruction = `
        <div class="input-group mb-2">
            <span class="input-group-text">${index}</span>
            <input type="text" class="form-control instruction-input" placeholder="Enter instruction step" value="${value}">
            <button type="button" class="btn btn-outline-danger" onclick="removeInstruction(this)">
                <i class="bi bi-trash"></i>
            </button>
        </div>
    `;
    $('#instructions-container').append(newInstruction);
}

function removeInstruction(button) {
    if ($('.instruction-input').length > 1) {
        $(button).closest('.input-group').remove();
        // Renumber remaining instructions
        $('.input-group-text').each((index, element) => {
            $(element).text(index + 1);
        });
    }
}

function handleSubmit(event) {
    event.preventDefault();

    const ingredients = Array.from($('.ingredient-input'))
        .map(input => input.value.trim())
        .filter(value => value !== '');

    const instructions = Array.from($('.instruction-input'))
        .map((input, index) => `${index + 1}. ${input.value.trim()}`)
        .filter(value => value !== '');

    if (ingredients.length === 0 || instructions.length === 0) {
        $("#error-container")
            .show()
            .text('Please add at least one ingredient and one instruction step.');
        return;
    }

    const recipe = {
        nombre: $('#nombre').val().trim(),
        tiempoPreparacion: parseInt($('#tiempoPreparacion').val()),
        dificultad: $('#dificultad').val(),
        listaIngredientes: ingredients,
        pasosPreparacion: instructions
    };

    if (recipeId) {
        recipe.id = recipeId;
    }

    const url = recipeId 
        ? `http://localhost:8080/api/recetas/actualizar/${recipeId}`
        : 'http://localhost:8080/api/recetas/crear';
    
    const method = recipeId ? 'PUT' : 'POST';

    $("#loading-spinner").show();
    $("#error-container").hide();

    $.ajax({
        url: url,
        method: method,
        crossDomain: true,
        xhrFields: {
            withCredentials: true
        },
        contentType: 'application/json',
        data: JSON.stringify(recipe),
        success: function(response) {
            window.location.href = `/recetas/view/${response.id}`;
        },
        error: function(xhr, status, error) {
            $("#loading-spinner").hide();
            $("#error-container")
                .show()
                .text('Error saving recipe. Please try again later. ' + error);
        }
    });
}