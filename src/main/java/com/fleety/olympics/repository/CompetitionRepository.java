package com.fleety.olympics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fleety.olympics.model.Competition;

@Repository
public interface CompetitionRepository extends JpaRepository<Competition, Long> {
    List<Competition> findByStatut(Competition.StatusCompetition statut);
    List<Competition> findByDiscipline(String discipline);
}