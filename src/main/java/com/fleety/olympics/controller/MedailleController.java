package com.fleety.olympics.controller;

import com.fleety.olympics.dto.request.MedailleRequestDTO;
import com.fleety.olympics.dto.response.ClassementResponseDTO;
import com.fleety.olympics.dto.response.MedailleResponseDTO;
import com.fleety.olympics.service.MedailleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MedailleController {

    private final MedailleService medailleService;

    @GetMapping("/api/v1/medailles")
    public List<MedailleResponseDTO> getAll() {
        return medailleService.getAll();
    }

    @GetMapping("/api/v1/medailles/{id}")
    public MedailleResponseDTO getById(@PathVariable Long id) {
        return medailleService.getById(id);
    }

    @GetMapping("/api/v1/medailles/athlete/{athleteId}")
    public List<MedailleResponseDTO> getByAthlete(@PathVariable Long athleteId) {
        return medailleService.getByAthlete(athleteId);
    }

    @GetMapping("/api/v1/medailles/competition/{competitionId}")
    public List<MedailleResponseDTO> getByCompetition(@PathVariable Long competitionId) {
        return medailleService.getByCompetition(competitionId);
    }

    @PostMapping("/api/v1/medailles")
    @ResponseStatus(HttpStatus.CREATED)
    public MedailleResponseDTO create(@Valid @RequestBody MedailleRequestDTO dto) {
        return medailleService.create(dto);
    }

    @DeleteMapping("/api/v1/medailles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        medailleService.delete(id);
    }

    // Classement
    @GetMapping("/api/v1/classement")
    public List<ClassementResponseDTO> getClassement(
            @RequestParam(required = false) String tri) {
        return medailleService.getClassement(tri);
    }

    @GetMapping("/api/v1/classement/pays/{paysId}")
    public ClassementResponseDTO getStatsByPays(@PathVariable Long paysId) {
        return medailleService.getStatsByPays(paysId);
    }
}