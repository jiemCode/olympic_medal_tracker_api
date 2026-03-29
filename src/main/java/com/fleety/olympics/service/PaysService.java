package com.fleety.olympics.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fleety.olympics.dto.request.PaysRequestDTO;
import com.fleety.olympics.dto.response.PageResponseDTO;
import com.fleety.olympics.dto.response.PaysResponseDTO;
import com.fleety.olympics.exception.DuplicateResourceException;
import com.fleety.olympics.exception.ResourceNotFoundException;
import com.fleety.olympics.model.Pays;
import com.fleety.olympics.repository.PaysRepository;
import com.fleety.olympics.service.interfaces.ReadableService;
import com.fleety.olympics.service.interfaces.WritableService;

import lombok.RequiredArgsConstructor;

/**
 * Service métier pour gérer les pays participants (CRUD + pagination).
 */
@Service
@RequiredArgsConstructor
public class PaysService implements ReadableService<PaysResponseDTO>, WritableService<PaysResponseDTO, PaysRequestDTO> {

    private final PaysRepository paysRepository;

    /**
     * Retourne une page de pays triés.
     *
     * @param pageable configuration de pagination/tri.
     * @return page de pays.
     */
    public PageResponseDTO<PaysResponseDTO> getAll(Pageable pageable) {
        return PageResponseDTO.from(
            paysRepository.findAll(pageable).map(this::toResponseDTO)
        );
    }

    /**
     * Détail d'un pays par identifiant.
     *
     * @param id identifiant du pays.
     * @return pays trouvé.
     */
    public PaysResponseDTO getById(Long id) {
        Pays pays = paysRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pays non trouvé avec l'id: " + id));
        return toResponseDTO(pays);
    }

    /**
     * Crée un pays en validant l'unicité du nom et du code.
     *
     * @param dto payload validé.
     * @return pays créé.
     * @throws DuplicateResourceException si nom ou code existent déjà.
     */
    public PaysResponseDTO create(PaysRequestDTO dto) {
        if (paysRepository.existsByCode(dto.getCode())) {
            throw new DuplicateResourceException("Un pays avec le code '" + dto.getCode() + "' existe déjà");
        }
        if (paysRepository.existsByNom(dto.getNom())) {
            throw new DuplicateResourceException("Un pays avec le nom '" + dto.getNom() + "' existe déjà");
        }
        return toResponseDTO(paysRepository.save(toEntity(dto)));
    }

    /**
     * Met à jour un pays existant.
     *
     * @param id identifiant du pays.
     * @param dto nouvelles valeurs.
     * @return pays mis à jour.
     */
    public PaysResponseDTO update(Long id, PaysRequestDTO dto) {
        Pays pays = paysRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pays non trouvé avec l'id: " + id));

        pays.setNom(dto.getNom());
        pays.setCode(dto.getCode());
        pays.setDrapeau(dto.getDrapeau());

        return toResponseDTO(paysRepository.save(pays));
    }

    /**
     * Supprime un pays par identifiant.
     *
     * @param id identifiant du pays.
     */
    public void delete(Long id) {
        if (!paysRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pays non trouvé avec l'id: " + id);
        }
        paysRepository.deleteById(id);
    }

    // Helpers
    private PaysResponseDTO toResponseDTO(Pays pays) {
        return new PaysResponseDTO(
                pays.getId(),
                pays.getNom(),
                pays.getCode(),
                pays.getDrapeau()
        );
    }

    private Pays toEntity(PaysRequestDTO dto) {
        Pays pays = new Pays();
        pays.setNom(dto.getNom());
        pays.setCode(dto.getCode());
        pays.setDrapeau(dto.getDrapeau());
        return pays;
    }
}
