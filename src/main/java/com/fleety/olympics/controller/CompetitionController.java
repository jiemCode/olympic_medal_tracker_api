package com.fleety.olympics.controller;

import com.fleety.olympics.dto.request.CompetitionRequestDTO;
import com.fleety.olympics.dto.response.CompetitionResponseDTO;
import com.fleety.olympics.service.CompetitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/competitions")
@RequiredArgsConstructor
public class CompetitionController {

    private final CompetitionService competitionService;

    @GetMapping
    public List<CompetitionResponseDTO> getAll() {
        return competitionService.getAll();
    }

    @GetMapping("/{id}")
    public CompetitionResponseDTO getById(@PathVariable Long id) {
        return competitionService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompetitionResponseDTO create(@Valid @RequestBody CompetitionRequestDTO dto) {
        return competitionService.create(dto);
    }

    @PutMapping("/{id}")
    public CompetitionResponseDTO update(@PathVariable Long id, @Valid @RequestBody CompetitionRequestDTO dto) {
        return competitionService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        competitionService.delete(id);
    }
}