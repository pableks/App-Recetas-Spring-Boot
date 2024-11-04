package com.example.RecetaApp.dto;



public class LoginResponse {
    private boolean success;
    private String message;
    private UserDTO user;
    
    // Default constructor
    public LoginResponse() {
    }
    
    // Constructor for error responses (no user)
    public LoginResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    // Constructor for successful responses with user
    public LoginResponse(boolean success, String message, UserDTO user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public UserDTO getUser() {
        return user;
    }
    
    public void setUser(UserDTO user) {
        this.user = user;
    }
}