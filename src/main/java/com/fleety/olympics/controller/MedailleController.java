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

/**
 * Gère les médailles, leur attribution et le classement
 * (chemin racine : {@code ${api.version}}).
 */
@RestController
@RequestMapping("${api.version}")
@RequiredArgsConstructor
public class MedailleController {

    private final ReadableService<MedailleResponseDTO> readableService;
    private final WritableService<MedailleResponseDTO, MedailleRequestDTO> writableService;
    private final Classifiable classifiable;
    private final MedailleFilterable medailleFilterable;

    /**
     * Liste paginée des médailles avec tri configurable.
     *
     * @param page numéro de page (0-indexé).
     * @param size nombre d'éléments par page.
     * @param sortBy champ de tri (ex. {@code dateObtention}, {@code type}).
     * @param direction sens du tri : {@code asc} (défaut) ou {@code desc}.
     * @return une page de médailles enregistrées.
     */
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

    /**
     * Détail d'une médaille.
     *
     * @param id identifiant de la médaille.
     * @return la médaille correspondante.
     */
    @GetMapping("/medailles/{id}")
    public MedailleResponseDTO getById(@PathVariable Long id) {
        return readableService.getById(id);
    }

    /**
     * Médailles remportées par un athlète.
     *
     * @param athleteId identifiant de l'athlète.
     * @return la liste des médailles attribuées à cet athlète.
     */
    @GetMapping("/medailles/athlete/{athleteId}")
    public List<MedailleResponseDTO> getByAthlete(@PathVariable Long athleteId) {
        return medailleFilterable.getByAthlete(athleteId);
    }

    /**
     * Médailles associées à une compétition.
     *
     * @param competitionId identifiant de la compétition.
     * @return les médailles attribuées dans cette compétition.
     */
    @GetMapping("/medailles/competition/{competitionId}")
    public List<MedailleResponseDTO> getByCompetition(@PathVariable Long competitionId) {
        return medailleFilterable.getByCompetition(competitionId);
    }

    /**
     * Attribue une nouvelle médaille.
     *
     * @param dto payload validé contenant le type ({@code OR}, {@code ARGENT}, {@code BRONZE}),
     *            la date et les identifiants de l'athlète, du pays et de la compétition.
     * @return la médaille créée.
     */
    @PostMapping("/medailles")
    @ResponseStatus(HttpStatus.CREATED)
    public MedailleResponseDTO create(@Valid @RequestBody MedailleRequestDTO dto) {
        return writableService.create(dto);
    }

    /**
     * Met à jour une médaille existante.
     *
     * @param id identifiant de la médaille à modifier.
     * @param dto nouvelles valeurs validées.
     * @return la médaille mise à jour.
     */
    @PutMapping("/medailles/{id}")
    public MedailleResponseDTO update(@PathVariable Long id, @Valid @RequestBody MedailleRequestDTO dto) {
        return writableService.update(id, dto);
    }

    /**
     * Supprime une médaille.
     *
     * @param id identifiant de la médaille à supprimer.
     */
    @DeleteMapping("/medailles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        writableService.delete(id);
    }

    // Classement
    /**
     * Retourne le classement des pays selon différents critères.
     *
     * @param tri stratégie de tri facultative : {@code or}, {@code argent},
     *            {@code bronze}, {@code points} (3/2/1) ou {@code total} (défaut).
     * @return la liste des pays ordonnés selon le critère choisi.
     */
    @GetMapping("/classement")
    public List<ClassementResponseDTO> getClassement(
            @RequestParam(required = false) String tri) {
        return classifiable.getClassement(tri);
    }

    /**
     * Statistiques de médailles pour un pays donné.
     *
     * @param paysId identifiant du pays.
     * @return le résumé des médailles (or/argent/bronze, total et points).
     */
    @GetMapping("/classement/pays/{paysId}")
    public ClassementResponseDTO getStatsByPays(@PathVariable Long paysId) {
        return classifiable.getStatsByPays(paysId);
    }
}
