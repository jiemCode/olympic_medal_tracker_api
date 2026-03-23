package com.fleety.olympics.model;


import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "medailles")
public class Medaille {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeMedaille type;

    @Column(nullable = false, columnDefinition = "DATE")
    private LocalDate dateObtention;

    @ManyToOne
    @JoinColumn(name = "athlete_id")
    private Athlete athlete;

    @ManyToOne
    @JoinColumn(name = "pays_id")
    private Pays pays;

    @ManyToOne
    @JoinColumn(name = "competition_id")
    private Competition competition;

    public enum TypeMedaille {
        OR, ARGENT, BRONZE
    }
}