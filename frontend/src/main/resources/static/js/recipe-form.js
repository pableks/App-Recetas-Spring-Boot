// Functions for managing dynamic form fields
function addIngredient() {
    const container = document.getElementById('ingredients-container');
    const index = container.children.length;
    const div = document.createElement('div');
    div.className = 'input-group mb-2';
    div.innerHTML = `
        <input type="text" 
               class="form-control" 
               name="listaIngredientes[${index}]" 
               placeholder="Ingrediente ${index + 1}" 
               required>
        <button type="button" class="btn btn-danger" onclick="removeField(this)">×</button>
    `;
    container.appendChild(div);
    updateIngredientIndices();
}

function addInstruction() {
    const container = document.getElementById('instructions-container');
    const index = container.children.length;
    const div = document.createElement('div');
    div.className = 'input-group mb-2';
    div.innerHTML = `
        <span class="input-group-text">${index + 1}</span>
        <input type="text" 
               class="form-control" 
               name="pasosPreparacion[${index}]" 
               placeholder="Paso ${index + 1}" 
               required>
        <button type="button" class="btn btn-danger" onclick="removeField(this)">×</button>
    `;
    container.appendChild(div);
    updateInstructionIndices();
}

function removeField(button) {
    const container = button.closest('.input-group');
    const parentContainer = container.parentElement;
    
    // Only remove if there's more than one field
    if (parentContainer.children.length > 1) {
        container.remove();
        
        if (parentContainer.id === 'ingredients-container') {
            updateIngredientIndices();
        } else {
            updateInstructionIndices();
        }
    }
}

function updateIngredientIndices() {
    const container = document.getElementById('ingredients-container');
    const inputs = container.getElementsByTagName('input');
    for (let i = 0; i < inputs.length; i++) {
        inputs[i].name = `listaIngredientes[${i}]`;
        inputs[i].placeholder = `Ingrediente ${i + 1}`;
    }
}

function updateInstructionIndices() {
    const container = document.getElementById('instructions-container');
    const steps = container.getElementsByClassName('input-group');
    for (let i = 0; i < steps.length; i++) {
        const input = steps[i].getElementsByTagName('input')[0];
        const numberSpan = steps[i].getElementsByClassName('input-group-text')[0];
        input.name = `pasosPreparacion[${i}]`;
        input.placeholder = `Paso ${i + 1}`;
        numberSpan.textContent = i + 1;
    }
}

// Initialize form when document is loaded
document.addEventListener('DOMContentLoaded', function() {
    // Add initial fields if it's a new recipe
    const ingredientsContainer = document.getElementById('ingredients-container');
    const instructionsContainer = document.getElementById('instructions-container');
    
    if (ingredientsContainer.children.length === 0) {
        addIngredient();
    }
    
    if (instructionsContainer.children.length === 0) {
        addInstruction();
    }
});