package com.fleety.olympics.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "athletes")
public class Athlete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "VARCHAR(100)")
    private String nom;

    @Column(nullable = false, columnDefinition = "VARCHAR(100)")
    private String prenom;

    @Column(nullable = false, columnDefinition = "DATE")
    private LocalDate dateNaissance;

    @Column(nullable = false, columnDefinition = "VARCHAR(100)")
    private String discipline;

    @ManyToOne
    @JoinColumn(name = "pays_id")
    private Pays pays;
}