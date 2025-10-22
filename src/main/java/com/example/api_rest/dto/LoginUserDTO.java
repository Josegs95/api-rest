package com.example.api_rest.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginUserDTO(

        @NotBlank
        String username,

        @NotBlank
        String password) {}
