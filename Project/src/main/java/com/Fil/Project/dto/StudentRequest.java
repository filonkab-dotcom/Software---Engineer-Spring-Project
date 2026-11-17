package com.Fil.Project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record StudentRequest(
        @NotBlank String universityId,
        @NotNull List<String> interests,
        @NotNull List<String> skills,
        String preferredLocation
) {}