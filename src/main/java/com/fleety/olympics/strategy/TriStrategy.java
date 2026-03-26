package com.fleety.olympics.strategy;

import java.util.Comparator;

import com.fleety.olympics.dto.response.ClassementResponseDTO;

public interface TriStrategy {

    Comparator<ClassementResponseDTO> comparator();
}