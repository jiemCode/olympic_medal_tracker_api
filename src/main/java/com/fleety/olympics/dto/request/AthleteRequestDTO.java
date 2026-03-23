package com.fleety.olympics.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AthleteRequestDTO {

    @NotBlank(message = "Le nom est requis")
    @Size(min = 2, message = "Le nom doit contenir au moins 02 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est requis")
    @Size(min = 2, message = "Le prénom doit contenir au moins 02 caractères")
    private String prenom;

    @NotNull(message = "La date de naissance est requise")
    private LocalDate dateNaissance;

    @NotBlank(message = "La discipline est requise")
    @Size(min = 3, message = "La discipline doit contenir au moins 03 caractères")
    private String discipline;

    @NotNull(message = "L'id du pays est requis")
    private Long paysId;
}