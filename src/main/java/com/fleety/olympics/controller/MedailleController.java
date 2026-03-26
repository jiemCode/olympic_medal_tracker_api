package com.fleety.olympics.controller;

import com.fleety.olympics.dto.request.MedailleRequestDTO;
import com.fleety.olympics.dto.response.ClassementResponseDTO;
import com.fleety.olympics.dto.response.MedailleResponseDTO;
import com.fleety.olympics.service.interfaces.Classifiable;
import com.fleety.olympics.service.interfaces.MedailleFilterable;
import com.fleety.olympics.service.interfaces.ReadableService;
import com.fleety.olympics.service.interfaces.WritableService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MedailleController {

    private final ReadableService<MedailleResponseDTO> readableService;
    private final WritableService<MedailleResponseDTO, MedailleRequestDTO> writableService;
    private final Classifiable classifiable;
    private final MedailleFilterable medailleFilterable;

    @GetMapping("/api/v1/medailles")
    public List<MedailleResponseDTO> getAll() {
        return readableService.getAll();
    }

    @GetMapping("/api/v1/medailles/{id}")
    public MedailleResponseDTO getById(@PathVariable Long id) {
        return readableService.getById(id);
    }

    @GetMapping("/api/v1/medailles/athlete/{athleteId}")
    public List<MedailleResponseDTO> getByAthlete(@PathVariable Long athleteId) {
        return medailleFilterable.getByAthlete(athleteId);
    }

    @GetMapping("/api/v1/medailles/competition/{competitionId}")
    public List<MedailleResponseDTO> getByCompetition(@PathVariable Long competitionId) {
        return medailleFilterable.getByCompetition(competitionId);
    }

    @PostMapping("/api/v1/medailles")
    @ResponseStatus(HttpStatus.CREATED)
    public MedailleResponseDTO create(@Valid @RequestBody MedailleRequestDTO dto) {
        return writableService.create(dto);
    }

    @PutMapping("/api/v1/medailles/{id}")
    public MedailleResponseDTO update(@PathVariable Long id, @Valid @RequestBody MedailleRequestDTO dto) {
        return writableService.update(id, dto);
    }

    @DeleteMapping("/api/v1/medailles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        writableService.delete(id);
    }

    // Classement
    @GetMapping("/api/v1/classement")
    public List<ClassementResponseDTO> getClassement(
            @RequestParam(required = false) String tri) {
        return classifiable.getClassement(tri);
    }

    @GetMapping("/api/v1/classement/pays/{paysId}")
    public ClassementResponseDTO getStatsByPays(@PathVariable Long paysId) {
        return classifiable.getStatsByPays(paysId);
    }
}