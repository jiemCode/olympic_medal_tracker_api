package com.fleety.olympics.service;

import com.fleety.olympics.dto.request.AthleteRequestDTO;
import com.fleety.olympics.dto.response.AthleteResponseDTO;
import com.fleety.olympics.exception.ResourceNotFoundException;
import com.fleety.olympics.model.Athlete;
import com.fleety.olympics.model.Pays;
import com.fleety.olympics.repository.AthleteRepository;
import com.fleety.olympics.repository.PaysRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AthleteService {

    private final AthleteRepository athleteRepository;
    private final PaysRepository paysRepository;

    public List<AthleteResponseDTO> getAll() {
        return athleteRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public AthleteResponseDTO getById(Long id) {
        return toResponseDTO(findOrThrow(id));
    }

    public List<AthleteResponseDTO> getByPays(Long paysId) {
        if (!paysRepository.existsById(paysId)) {
            throw new ResourceNotFoundException("Pays non trouvé avec l'id: " + paysId);
        }
        return athleteRepository.findByPaysId(paysId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public AthleteResponseDTO create(AthleteRequestDTO dto) {
        Pays pays = paysRepository.findById(dto.getPaysId())
                .orElseThrow(() -> new ResourceNotFoundException("Pays non trouvé avec l'id: " + dto.getPaysId()));
        Athlete athlete = toEntity(dto, pays);
        return toResponseDTO(athleteRepository.save(athlete));
    }

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