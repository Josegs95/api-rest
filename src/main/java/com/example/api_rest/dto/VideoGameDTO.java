package com.example.api_rest.dto;

import com.example.api_rest.entity.Genre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record VideoGameDTO(
        @NotBlank
        String name,

        @PastOrPresent
        LocalDate releaseDate,

        @NotNull
        String developedBy,


        Genre genre) {

}
