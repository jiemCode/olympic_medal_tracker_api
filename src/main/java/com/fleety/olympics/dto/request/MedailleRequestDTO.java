package com.fleety.olympics.dto.request;

import java.time.LocalDate;

import com.fleety.olympics.model.Medaille.TypeMedaille;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MedailleRequestDTO {

    @NotNull(message = "Le type est requis")
    private TypeMedaille type;

    @NotNull(message = "La date est requise")
    private LocalDate dateObtention;

    @NotNull(message = "L'athlète doit être spécifié")
    private Long athleteId;

    @NotNull(message = "Le pays doit être spécifié")
    private Long paysId;

    @NotNull(message = "La compétition doit être spécifié")
    private Long competitionId;
}