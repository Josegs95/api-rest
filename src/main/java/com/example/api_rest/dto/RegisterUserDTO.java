package com.example.api_rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterUserDTO(

        @NotBlank
        String username,

        @NotNull
        String password,

        String email){
}
