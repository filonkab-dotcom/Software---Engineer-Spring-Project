package com.Fil.Project.dto;

import jakarta.validation.constraints.NotBlank;

public record CompanyProfileRequest(
        @NotBlank String companyName,
        String location,
        String firstName,
        String lastName,
        String email,
        String description
) {}