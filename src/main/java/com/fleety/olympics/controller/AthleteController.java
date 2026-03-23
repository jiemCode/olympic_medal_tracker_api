package com.fleety.olympics.controller;

import com.fleety.olympics.dto.request.AthleteRequestDTO;
import com.fleety.olympics.dto.response.AthleteResponseDTO;
import com.fleety.olympics.service.AthleteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/athletes")
@RequiredArgsConstructor
public class AthleteController {

    private final AthleteService athleteService;

    @GetMapping
    public List<AthleteResponseDTO> getAll() {
        return athleteService.getAll();
    }

    @GetMapping("/{id}")
    public AthleteResponseDTO getById(@PathVariable Long id) {
        return athleteService.getById(id);
    }

    @GetMapping("/pays/{paysId}")
    public List<AthleteResponseDTO> getByPays(@PathVariable Long paysId) {
        return athleteService.getByPays(paysId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AthleteResponseDTO create(@Valid @RequestBody AthleteRequestDTO dto) {
        return athleteService.create(dto);
    }

    @PutMapping("/{id}")
    public AthleteResponseDTO update(@PathVariable Long id, @Valid @RequestBody AthleteRequestDTO dto) {
        return athleteService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        athleteService.delete(id);
    }
}