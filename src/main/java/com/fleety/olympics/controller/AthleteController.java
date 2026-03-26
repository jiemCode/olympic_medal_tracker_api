package com.fleety.olympics.controller;

import com.fleety.olympics.dto.request.AthleteRequestDTO;
import com.fleety.olympics.dto.response.AthleteResponseDTO;
import com.fleety.olympics.service.interfaces.Filterable;
import com.fleety.olympics.service.interfaces.ReadableService;
import com.fleety.olympics.service.interfaces.WritableService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/athletes")
@RequiredArgsConstructor
public class AthleteController {

    private final ReadableService<AthleteResponseDTO> readableService;
    private final WritableService<AthleteResponseDTO, AthleteRequestDTO> writableService;
    private final Filterable<AthleteResponseDTO> filterable;

    @GetMapping
    public List<AthleteResponseDTO> getAll() {
        return readableService.getAll();
    }

    @GetMapping("/{id}")
    public AthleteResponseDTO getById(@PathVariable Long id) {
        return readableService.getById(id);
    }

    @GetMapping("/pays/{paysId}")
    public List<AthleteResponseDTO> getByPays(@PathVariable Long paysId) {
        return filterable.getByPays(paysId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AthleteResponseDTO create(@Valid @RequestBody AthleteRequestDTO dto) {
        return writableService.create(dto);
    }

    @PutMapping("/{id}")
    public AthleteResponseDTO update(@PathVariable Long id, @Valid @RequestBody AthleteRequestDTO dto) {
        return writableService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        writableService.delete(id);
    }
}