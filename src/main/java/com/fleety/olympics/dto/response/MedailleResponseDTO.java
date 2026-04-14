package com.fleety.olympics.dto.response;

import java.time.LocalDate;

import com.fleety.olympics.model.Medaille.TypeMedaille;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MedailleResponseDTO {

    private Long id;
    private TypeMedaille type;
    private LocalDate dateObtention;
    private Long athleteId;
    private String athleteNom;
    private String athletePrenom;
    private Long paysId;
    private String paysNom;
    private Long competitionId;
    private String competitionNom;
}