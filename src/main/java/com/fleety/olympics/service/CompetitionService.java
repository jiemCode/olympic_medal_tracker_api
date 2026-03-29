package com.fleety.olympics.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fleety.olympics.dto.request.CompetitionRequestDTO;
import com.fleety.olympics.dto.response.CompetitionResponseDTO;
import com.fleety.olympics.dto.response.PageResponseDTO;
import com.fleety.olympics.exception.DuplicateResourceException;
import com.fleety.olympics.exception.ResourceNotFoundException;
import com.fleety.olympics.model.Competition;
import com.fleety.olympics.repository.CompetitionRepository;
import com.fleety.olympics.service.interfaces.ReadableService;
import com.fleety.olympics.service.interfaces.WritableService;

import lombok.RequiredArgsConstructor;

/**
 * Service métier pour gérer le cycle de vie des compétitions
 * (listing paginé, détail, création, mise à jour, suppression).
 */
@Service
@RequiredArgsConstructor
public class CompetitionService implements ReadableService<CompetitionResponseDTO>, WritableService<CompetitionResponseDTO, CompetitionRequestDTO> {

    private final CompetitionRepository competitionRepository;

    /**
     * Retourne une page de compétitions avec tri appliqué en base.
     *
     * @param pageable configuration de pagination/tri.
     * @return page de {@link CompetitionResponseDTO}.
     */
    public PageResponseDTO<CompetitionResponseDTO> getAll(Pageable pageable) {
        return PageResponseDTO.from(
            competitionRepository.findAll(pageable).map(this::toResponseDTO)
        );
    }

    /**
     * Récupère une compétition par identifiant.
     *
     * @param id identifiant de la compétition.
     * @return DTO correspondant.
     */
    public CompetitionResponseDTO getById(Long id) {
        return toResponseDTO(findOrThrow(id));
    }

    /**
     * Crée une compétition si le nom est unique.
     *
     * @param dto payload validé.
     * @return compétition créée.
     * @throws DuplicateResourceException si le nom existe déjà.
     */
    public CompetitionResponseDTO create(CompetitionRequestDTO dto) {
        if (competitionRepository.existsByNom(dto.getNom())) {
            throw new DuplicateResourceException("Une compétition avec le nom '" + dto.getNom() + "' existe déjà");
        }
        return toResponseDTO(competitionRepository.save(toEntity(dto)));
    }

    /**
     * Met à jour une compétition existante.
     *
     * @param id identifiant de la compétition.
     * @param dto nouvelles valeurs.
     * @return compétition mise à jour.
     */
    public CompetitionResponseDTO update(Long id, CompetitionRequestDTO dto) {
        Competition competition = findOrThrow(id);

        competition.setNom(dto.getNom());
        competition.setDiscipline(dto.getDiscipline());
        competition.setDateDebut(dto.getDateDebut());
        competition.setDateFin(dto.getDateFin());
        competition.setStatut(dto.getStatut());

        return toResponseDTO(competitionRepository.save(competition));
    }

    /**
     * Supprime une compétition par identifiant.
     *
     * @param id identifiant de la compétition.
     */
    public void delete(Long id) {
        if (!competitionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Compétition non trouvée avec l'id: " + id);
        }
        competitionRepository.deleteById(id);
    }

    // Helpers
    private Competition findOrThrow(Long id) {
        return competitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compétition non trouvée avec l'id: " + id));
    }

    private CompetitionResponseDTO toResponseDTO(Competition c) {
        return new CompetitionResponseDTO(
                c.getId(),
                c.getNom(),
                c.getDiscipline(),
                c.getDateDebut(),
                c.getDateFin(),
                c.getStatut()
        );
    }

    private Competition toEntity(CompetitionRequestDTO dto) {
        Competition c = new Competition();
        c.setNom(dto.getNom());
        c.setDiscipline(dto.getDiscipline());
        c.setDateDebut(dto.getDateDebut());
        c.setDateFin(dto.getDateFin());
        c.setStatut(dto.getStatut());
        return c;
    }
}
