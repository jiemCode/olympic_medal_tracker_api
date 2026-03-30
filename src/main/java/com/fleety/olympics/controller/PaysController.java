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

import com.fleety.olympics.dto.request.PaysRequestDTO;
import com.fleety.olympics.dto.response.PageResponseDTO;
import com.fleety.olympics.dto.response.PaysResponseDTO;
import com.fleety.olympics.service.interfaces.ReadableService;
import com.fleety.olympics.service.interfaces.WritableService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Expose les endpoints CRUD pour les pays participants
 * (chemin racine : {@code ${api.version}/pays}).
 */
@RestController
@RequestMapping("${api.version}/pays")
@RequiredArgsConstructor
public class PaysController {

    private final ReadableService<PaysResponseDTO> readableService;
    private final WritableService<PaysResponseDTO, PaysRequestDTO> writableService;

    /**
     * Liste paginée des pays avec tri configurable.
     *
     * @param page numéro de page (0-indexé).
     * @param size nombre de pays par page.
     * @param sortBy champ de tri (ex. {@code nom}, {@code code}).
     * @param direction sens du tri : {@code asc} (défaut) ou {@code desc}.
     * @return une page de pays.
     */
    @GetMapping
    public PageResponseDTO<PaysResponseDTO> getAll(
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

    /**
     * Détail d'un pays.
     *
     * @param id identifiant du pays.
     * @return le pays correspondant.
     */
    @GetMapping("/{id}")
    public PaysResponseDTO getById(@PathVariable Long id) {
        return readableService.getById(id);
    }

    /**
     * Crée un pays (code ISO unique sur 2-3 caractères).
     *
     * @param dto payload validé du pays à créer.
     * @return le pays créé.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaysResponseDTO create(@Valid @RequestBody PaysRequestDTO dto) {
        return writableService.create(dto);
    }

    /**
     * Met à jour un pays existant.
     *
     * @param id identifiant du pays à modifier.
     * @param dto nouvelles valeurs validées.
     * @return le pays mis à jour.
     */
    @PutMapping("/{id}")
    public PaysResponseDTO update(@PathVariable Long id, @Valid @RequestBody PaysRequestDTO dto) {
        return writableService.update(id, dto);
    }

    /**
     * Supprime un pays.
     *
     * @param id identifiant du pays à supprimer.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        writableService.delete(id);
    }
}
