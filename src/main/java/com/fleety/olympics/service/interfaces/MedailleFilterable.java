package com.fleety.olympics.service.interfaces;

import com.fleety.olympics.dto.response.MedailleResponseDTO;
import java.util.List;

public interface MedailleFilterable {
    List<MedailleResponseDTO> getByAthlete(Long athleteId);
    List<MedailleResponseDTO> getByCompetition(Long competitionId);
}