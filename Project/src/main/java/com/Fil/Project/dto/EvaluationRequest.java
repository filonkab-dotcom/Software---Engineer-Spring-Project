package com.Fil.Project.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record EvaluationRequest(
        @Min(1) @Max(5) int studentMotivation,
        @Min(1) @Max(5) int studentEffectiveness,
        @Min(1) @Max(5) int studentEfficiency,
        @Min(1) @Max(5) int companyFacilities,
        @Min(1) @Max(5) int companyGuidance
) {}