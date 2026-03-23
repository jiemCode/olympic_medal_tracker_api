package com.fleety.olympics.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "competitions")
public class Competition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, columnDefinition = "VARCHAR(200)")
    private String nom;

    @Column(nullable = false, columnDefinition = "VARCHAR(100)")
    private String discipline;

    @Column(nullable = false, columnDefinition = "DATE")
    private LocalDate dateDebut;

    @Column(nullable = false, columnDefinition = "DATE")
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusCompetition statut;

    public enum StatusCompetition {
        PLANIFIEE, EN_COURS, TERMINEE
    }
}