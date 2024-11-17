package com.example.recetasfrontend.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private boolean success;
    private String message;
    private User user;
    private String authToken;
    private String sessionId;  // Added this field

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User {
        private Long id;
        private String username;
        private List<String> roles;
    }
}