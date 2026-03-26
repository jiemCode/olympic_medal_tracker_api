package com.fleety.olympics.service.interfaces;

import java.util.List;

import com.fleety.olympics.dto.response.ClassementResponseDTO;

public interface Classifiable {
    List<ClassementResponseDTO> getClassement(String tri);
    ClassementResponseDTO getStatsByPays(Long paysId);
}
