package com.fleety.olympics.controller;

import com.fleety.olympics.dto.request.PaysRequestDTO;
import com.fleety.olympics.dto.response.PaysResponseDTO;
import com.fleety.olympics.service.interfaces.ReadableService;
import com.fleety.olympics.service.interfaces.WritableService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pays")
@RequiredArgsConstructor
public class PaysController {

    private final ReadableService<PaysResponseDTO> readableService;
    private final WritableService<PaysResponseDTO, PaysRequestDTO> writableService;

    @GetMapping
    public List<PaysResponseDTO> getAll() {
        return readableService.getAll();
    }

    @GetMapping("/{id}")
    public PaysResponseDTO getById(@PathVariable Long id) {
        return readableService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaysResponseDTO create(@Valid @RequestBody PaysRequestDTO dto) {
        return writableService.create(dto);
    }

    @PutMapping("/{id}")
    public PaysResponseDTO update(@PathVariable Long id, @Valid @RequestBody PaysRequestDTO dto) {
        return writableService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        writableService.delete(id);
    }
}