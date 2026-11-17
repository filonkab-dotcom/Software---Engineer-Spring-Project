package com.Fil.Project.dto;

public record UserRegistrationRequest(String firstName, String lastName, String email, String password) {
    public UserRegistrationRequest {
        if (password.length() < 8 || !password.matches(".*[0-9].*") || !password.matches(".*[A-Za-z].*")) {
            throw new IllegalArgumentException("Password must be ≥8 chars and alphanumeric");
        }
    }
}