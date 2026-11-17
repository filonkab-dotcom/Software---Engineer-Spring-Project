package com.Fil.Project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record PositionAnnouncementRequest(
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotBlank String description,
        @NotEmpty List<String> requiredSkills,
        @NotEmpty List<String> topics
) {}