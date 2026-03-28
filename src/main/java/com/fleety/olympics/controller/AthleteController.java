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

import com.fleety.olympics.dto.request.AthleteRequestDTO;
import com.fleety.olympics.dto.response.AthleteResponseDTO;
import com.fleety.olympics.dto.response.PageResponseDTO;
import com.fleety.olympics.service.interfaces.Filterable;
import com.fleety.olympics.service.interfaces.ReadableService;
import com.fleety.olympics.service.interfaces.WritableService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.version}/athletes")
@RequiredArgsConstructor
public class AthleteController {

    private final ReadableService<AthleteResponseDTO> readableService;
    private final WritableService<AthleteResponseDTO, AthleteRequestDTO> writableService;
    private final Filterable<AthleteResponseDTO> filterable;

    @GetMapping
    public PageResponseDTO<AthleteResponseDTO> getAll(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(defaultValue = "nom") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return readableService.getAll(pageable);
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