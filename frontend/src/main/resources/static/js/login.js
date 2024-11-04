// src/main/resources/static/js/login.js
function handleLogin(event) {
    event.preventDefault();
    
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    
    $.ajax({
        url: 'http://localhost:8080/auth/login',
        method: 'POST',
        crossDomain: true,
        xhrFields: {
            withCredentials: true
        },
        contentType: 'application/json',
        data: JSON.stringify({
            username: username,
            password: password
        }),
        success: function(response) {
            console.log('Login successful');
            // Trigger custom event for navbar update
            window.dispatchEvent(new Event('loginSuccess'));
            // Redirect to recipes page
            window.location.href = '/recetas';
        },
        error: function(xhr, status, error) {
            console.error('Login error:', error);
            document.getElementById('errorMessage').style.display = 'block';
            document.getElementById('errorMessage').textContent = 'Invalid username or password';
        }
    });
}

// Attach the handler when the document is ready
$(document).ready(function() {
    $('#loginForm').on('submit', handleLogin);
});