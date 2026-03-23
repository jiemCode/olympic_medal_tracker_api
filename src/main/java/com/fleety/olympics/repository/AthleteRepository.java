package com.fleety.olympics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fleety.olympics.model.Athlete;

@Repository
public interface AthleteRepository extends JpaRepository<Athlete, Long> {
    List<Athlete> findByPaysId(Long paysId);
}