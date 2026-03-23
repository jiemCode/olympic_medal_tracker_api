package com.fleety.olympics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fleety.olympics.model.Pays;

@Repository
public interface PaysRepository extends JpaRepository<Pays, Long> {
    boolean existsByNom(String nom);
    boolean existsByCode(String code);
}