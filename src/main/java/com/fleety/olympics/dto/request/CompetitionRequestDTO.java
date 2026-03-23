package com.fleety.olympics.dto.request;

import java.time.LocalDate;

import com.fleety.olympics.model.Competition.StatusCompetition;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompetitionRequestDTO {

    @NotBlank(message = "Le nom est requis")
    @Size(min = 3, message = "Le nom doit contenir au moins 3 caractères")
    private String nom;

    @NotBlank(message = "La discipline est requise")
    @Size(min = 3, message = "La discipline doit contenir au moins 3 caractères")
    private String discipline;

    @NotNull(message = "La date de début est requise")
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est requise")
    private LocalDate dateFin;

    @NotNull(message = "Le statut est requis (PLANIFIEE, EN_COURS, TERMINEE")
    private StatusCompetition statut;
}