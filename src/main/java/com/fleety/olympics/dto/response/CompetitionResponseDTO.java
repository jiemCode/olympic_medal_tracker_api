package com.fleety.olympics.dto.response;

import java.time.LocalDate;

import com.fleety.olympics.model.Competition.StatusCompetition;

import lombok.Data;

@Data
public class CompetitionResponseDTO {

    private Long id;
    private String nom;
    private String discipline;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private StatusCompetition statut;
}