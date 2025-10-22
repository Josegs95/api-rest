package com.example.api_rest.dto;

import com.example.api_rest.entity.Role;
import jakarta.validation.constraints.NotBlank;

public record EditUserDTO(

        @NotBlank
        String username,

        String password,

        String email,

        Role role) {
}
