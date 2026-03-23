package com.fleety.olympics.dto.response;

import java.time.LocalDate;

import com.fleety.olympics.model.Medaille.TypeMedaille;

import lombok.Data;

@Data
public class MedailleResponseDTO {

    private Long id;
    private TypeMedaille type;
    private LocalDate dateObtention;
    private String athleteNom;
    private String athletePrenom;
    private String paysNom;
    private String competitionNom;
}