package com.fleety.olympics.controller;

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

import com.fleety.olympics.dto.request.CompetitionRequestDTO;
import com.fleety.olympics.dto.response.CompetitionResponseDTO;
import com.fleety.olympics.dto.response.PageResponseDTO;
import com.fleety.olympics.service.interfaces.ReadableService;
import com.fleety.olympics.service.interfaces.WritableService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("${api.version}/competitions")
@RequiredArgsConstructor
public class CompetitionController {

    private final ReadableService<CompetitionResponseDTO> readableService;
    private final WritableService<CompetitionResponseDTO, CompetitionRequestDTO> writableService;


    @GetMapping
    public PageResponseDTO<CompetitionResponseDTO> getAll(
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
    public CompetitionResponseDTO getById(@PathVariable Long id) {
        return readableService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompetitionResponseDTO create(@Valid @RequestBody CompetitionRequestDTO dto) {
        return writableService.create(dto);
    }

    @PutMapping("/{id}")
    public CompetitionResponseDTO update(@PathVariable Long id, @Valid @RequestBody CompetitionRequestDTO dto) {
        return writableService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        writableService.delete(id);
    }
}