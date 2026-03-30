package com.fleety.olympics.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fleety.olympics.dto.request.AthleteRequestDTO;
import com.fleety.olympics.dto.response.AthleteResponseDTO;
import com.fleety.olympics.dto.response.PageResponseDTO;
import com.fleety.olympics.exception.ResourceNotFoundException;
import com.fleety.olympics.model.Athlete;
import com.fleety.olympics.model.Pays;
import com.fleety.olympics.repository.AthleteRepository;
import com.fleety.olympics.repository.PaysRepository;
import com.fleety.olympics.service.interfaces.Filterable;
import com.fleety.olympics.service.interfaces.ReadableService;
import com.fleety.olympics.service.interfaces.WritableService;

import lombok.RequiredArgsConstructor;

/**
 * Service métier pour gérer les athlètes (CRUD, pagination, filtrage par pays).
 */
@Service
@RequiredArgsConstructor
public class AthleteService implements ReadableService<AthleteResponseDTO>, WritableService<AthleteResponseDTO, AthleteRequestDTO>, Filterable<AthleteResponseDTO> {

    private final AthleteRepository athleteRepository;
    private final PaysRepository paysRepository;

    /**
     * Retourne une page d'athlètes avec tri appliqué.
     *
     * @param pageable configuration pagination/tri.
     * @return page d'athlètes.
     */
    public PageResponseDTO<AthleteResponseDTO> getAll(Pageable pageable) {
        return PageResponseDTO.from(
            athleteRepository.findAll(pageable).map(this::toResponseDTO)
        );
    }

    /**
     * Récupère un athlète par identifiant.
     *
     * @param id identifiant de l'athlète.
     * @return DTO correspondant.
     */
    public AthleteResponseDTO getById(Long id) {
        return toResponseDTO(findOrThrow(id));
    }

    /**
     * Liste les athlètes d'un pays.
     *
     * @param paysId identifiant du pays.
     * @return athlètes rattachés.
     */
    public List<AthleteResponseDTO> getByPays(Long paysId) {
        if (!paysRepository.existsById(paysId)) {
            throw new ResourceNotFoundException("Pays non trouvé avec l'id: " + paysId);
        }
        return athleteRepository.findByPaysId(paysId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Crée un athlète après validation du pays cible.
     *
     * @param dto payload validé.
     * @return athlète créé.
     */
    public AthleteResponseDTO create(AthleteRequestDTO dto) {
        Pays pays = paysRepository.findById(dto.getPaysId())
                .orElseThrow(() -> new ResourceNotFoundException("Pays non trouvé avec l'id: " + dto.getPaysId()));
        Athlete athlete = toEntity(dto, pays);
        return toResponseDTO(athleteRepository.save(athlete));
    }

    /**
     * Met à jour un athlète existant.
     *
     * @param id identifiant de l'athlète.
     * @param dto nouvelles valeurs.
     * @return athlète mis à jour.
     */
    public AthleteResponseDTO update(Long id, AthleteRequestDTO dto) {
        Athlete athlete = findOrThrow(id);
        Pays pays = paysRepository.findById(dto.getPaysId())
                .orElseThrow(() -> new ResourceNotFoundException("Pays non trouvé avec l'id: " + dto.getPaysId()));

        athlete.setNom(dto.getNom());
        athlete.setPrenom(dto.getPrenom());
        athlete.setDateNaissance(dto.getDateNaissance());
        athlete.setDiscipline(dto.getDiscipline());
        athlete.setPays(pays);

        return toResponseDTO(athleteRepository.save(athlete));
    }

    /**
     * Supprime un athlète par identifiant.
     *
     * @param id identifiant de l'athlète.
     */
    public void delete(Long id) {
        if (!athleteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Athlète non trouvé avec l'id: " + id);
        }
        athleteRepository.deleteById(id);
    }

    // Helpers
    private Athlete findOrThrow(Long id) {
        return athleteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Athlète non trouvé avec l'id: " + id));
    }

    private AthleteResponseDTO toResponseDTO(Athlete a) {
        return new AthleteResponseDTO(
                a.getId(),
                a.getNom(),
                a.getPrenom(),
                a.getDateNaissance(),
                a.getDiscipline(),
                a.getPays().getId(),
                a.getPays().getNom(),
                a.getPays().getCode()
        );
    }

    private Athlete toEntity(AthleteRequestDTO dto, Pays pays) {
        Athlete a = new Athlete();
        a.setNom(dto.getNom());
        a.setPrenom(dto.getPrenom());
        a.setDateNaissance(dto.getDateNaissance());
        a.setDiscipline(dto.getDiscipline());
        a.setPays(pays);
        return a;
    }
}
