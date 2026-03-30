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

/**
 * Endpoints CRUD pour gérer les compétitions olympiques
 * (chemin racine : {@code ${api.version}/competitions}).
 */
@RestController
@RequestMapping("${api.version}/competitions")
@RequiredArgsConstructor
public class CompetitionController {

    private final ReadableService<CompetitionResponseDTO> readableService;
    private final WritableService<CompetitionResponseDTO, CompetitionRequestDTO> writableService;

    /**
     * Liste paginée des compétitions avec tri dynamique.
     *
     * @param page numéro de page (0-indexé).
     * @param size nombre d'éléments par page.
     * @param sortBy propriété utilisée pour le tri (ex. {@code nom}, {@code dateDebut}).
     * @param direction sens du tri : {@code asc} (défaut) ou {@code desc}.
     * @return une page de compétitions et ses métadonnées.
     */
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

    /**
     * Récupère une compétition par son identifiant.
     *
     * @param id identifiant unique de la compétition.
     * @return la compétition trouvée.
     */
    @GetMapping("/{id}")
    public CompetitionResponseDTO getById(@PathVariable Long id) {
        return readableService.getById(id);
    }

    /**
     * Crée une nouvelle compétition.
     *
     * @param dto données de la compétition à créer (nom, discipline, dates, statut).
     * @return la compétition créée.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompetitionResponseDTO create(@Valid @RequestBody CompetitionRequestDTO dto) {
        return writableService.create(dto);
    }

    /**
     * Met à jour une compétition existante.
     *
     * @param id identifiant de la compétition à modifier.
     * @param dto payload validé contenant les nouvelles valeurs.
     * @return la compétition mise à jour.
     */
    @PutMapping("/{id}")
    public CompetitionResponseDTO update(@PathVariable Long id, @Valid @RequestBody CompetitionRequestDTO dto) {
        return writableService.update(id, dto);
    }

    /**
     * Supprime une compétition.
     *
     * @param id identifiant de la compétition à supprimer.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        writableService.delete(id);
    }
}
