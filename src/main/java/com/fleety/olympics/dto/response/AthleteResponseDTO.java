package com.fleety.olympics.dto.response;

import java.time.LocalDate;

import lombok.Data;

@Data
public class AthleteResponseDTO {

    private Long id;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String discipline;
    private Long paysId;
    private String paysNom;
    private String paysCode;
}