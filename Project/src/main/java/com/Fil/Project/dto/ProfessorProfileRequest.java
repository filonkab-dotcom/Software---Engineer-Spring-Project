package com.Fil.Project.dto;

import java.util.List;

public record ProfessorProfileRequest(
        String firstName,
        String lastName,
        String email,
        List<String> interests
) {}