package com.fleety.olympics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fleety.olympics.model.Medaille;

@Repository
public interface MedailleRepository extends JpaRepository<Medaille, Long> {
    List<Medaille> findByAthleteId(Long athleteId);
    List<Medaille> findByPaysId(Long paysId);
    List<Medaille> findByCompetitionId(Long competitionId);
}