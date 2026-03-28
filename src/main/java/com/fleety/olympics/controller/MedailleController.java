package com.fleety.olympics.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fleety.olympics.dto.request.MedailleRequestDTO;
import com.fleety.olympics.dto.response.ClassementResponseDTO;
import com.fleety.olympics.dto.response.MedailleResponseDTO;
import com.fleety.olympics.dto.response.PageResponseDTO;
import com.fleety.olympics.service.interfaces.Classifiable;
import com.fleety.olympics.service.interfaces.MedailleFilterable;
import com.fleety.olympics.service.interfaces.ReadableService;
import com.fleety.olympics.service.interfaces.WritableService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.version}")
@RequiredArgsConstructor
public class MedailleController {

    private final ReadableService<MedailleResponseDTO> readableService;
    private final WritableService<MedailleResponseDTO, MedailleRequestDTO> writableService;
    private final Classifiable classifiable;
    private final MedailleFilterable medailleFilterable;

    @GetMapping("/medailles")
    public PageResponseDTO<MedailleResponseDTO> getAll(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(defaultValue = "dateObtention") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return readableService.getAll(pageable);
    }

    @GetMapping("/medailles/{id}")
    public MedailleResponseDTO getById(@PathVariable Long id) {
        return readableService.getById(id);
    }

    @GetMapping("/medailles/athlete/{athleteId}")
    public List<MedailleResponseDTO> getByAthlete(@PathVariable Long athleteId) {
        return medailleFilterable.getByAthlete(athleteId);
    }

    @GetMapping("/medailles/competition/{competitionId}")
    public List<MedailleResponseDTO> getByCompetition(@PathVariable Long competitionId) {
        return medailleFilterable.getByCompetition(competitionId);
    }

    @PostMapping("/medailles")
    @ResponseStatus(HttpStatus.CREATED)
    public MedailleResponseDTO create(@Valid @RequestBody MedailleRequestDTO dto) {
        return writableService.create(dto);
    }

    @PutMapping("/medailles/{id}")
    public MedailleResponseDTO update(@PathVariable Long id, @Valid @RequestBody MedailleRequestDTO dto) {
        return writableService.update(id, dto);
    }

    @DeleteMapping("/medailles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        writableService.delete(id);
    }

    // Classement
    @GetMapping("/classement")
    public List<ClassementResponseDTO> getClassement(
            @RequestParam(required = false) String tri) {
        return classifiable.getClassement(tri);
    }

    @GetMapping("/classement/pays/{paysId}")
    public ClassementResponseDTO getStatsByPays(@PathVariable Long paysId) {
        return classifiable.getStatsByPays(paysId);
    }
}
