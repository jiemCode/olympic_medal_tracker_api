package com.fleety.olympics.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fleety.olympics.model.Competition;

@Repository
public interface CompetitionRepository extends JpaRepository<Competition, Long> {
    Page<Competition> findByStatut(Competition.StatusCompetition statut, Pageable pageable);
    List<Competition> findByDiscipline(String discipline);
    boolean existsByNom(String nom);
}