package com.fleety.olympics.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PaysRequestDTO {

    @NotBlank(message = "Le nom du pays est requis")
    private String nom;

    @NotBlank(message = "Le code du pays est requis")
    private String code;

    private String drapeau;
}