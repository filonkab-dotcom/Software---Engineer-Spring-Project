package com.Fil.Project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record LogbookEntryRequest(
        @NotNull LocalDate entryDate,
        @NotBlank String description
) {}
