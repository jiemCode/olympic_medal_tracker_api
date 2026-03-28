package com.fleety.olympics.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fleety.olympics.dto.request.MedailleRequestDTO;
import com.fleety.olympics.dto.response.ClassementResponseDTO;
import com.fleety.olympics.dto.response.MedailleResponseDTO;
import com.fleety.olympics.dto.response.PageResponseDTO;
import com.fleety.olympics.exception.ResourceNotFoundException;
import com.fleety.olympics.model.Athlete;
import com.fleety.olympics.model.Competition;
import com.fleety.olympics.model.Medaille;
import com.fleety.olympics.model.Medaille.TypeMedaille;
import com.fleety.olympics.model.Pays;
import com.fleety.olympics.repository.AthleteRepository;
import com.fleety.olympics.repository.CompetitionRepository;
import com.fleety.olympics.repository.MedailleRepository;
import com.fleety.olympics.repository.PaysRepository;
import com.fleety.olympics.service.interfaces.Classifiable;
import com.fleety.olympics.service.interfaces.MedailleFilterable;
import com.fleety.olympics.service.interfaces.ReadableService;
import com.fleety.olympics.service.interfaces.WritableService;
import com.fleety.olympics.strategy.TriStrategy;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MedailleService implements ReadableService<MedailleResponseDTO>, WritableService<MedailleResponseDTO, MedailleRequestDTO>, Classifiable, MedailleFilterable{

    private final MedailleRepository medailleRepository;
    private final AthleteRepository athleteRepository;
    private final PaysRepository paysRepository;
    private final CompetitionRepository competitionRepository;
    private final Map<String, TriStrategy> triStrategies;  // Injection des méthodes de tri

    public PageResponseDTO<MedailleResponseDTO> getAll(Pageable pageable) {
        return PageResponseDTO.from(
            medailleRepository.findAll(pageable).map(this::toResponseDTO)
        );
    }

    public MedailleResponseDTO getById(Long id) {
        return toResponseDTO(findOrThrow(id));
    }

    public List<MedailleResponseDTO> getByAthlete(Long athleteId) {
        if (!athleteRepository.existsById(athleteId)) {
            throw new ResourceNotFoundException("Athlète non trouvé avec l'id: " + athleteId);
        }
        return medailleRepository.findByAthleteId(athleteId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public List<MedailleResponseDTO> getByCompetition(Long competitionId) {
        if (!competitionRepository.existsById(competitionId)) {
            throw new ResourceNotFoundException("Compétition non trouvée avec l'id: " + competitionId);
        }
        return medailleRepository.findByCompetitionId(competitionId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public MedailleResponseDTO create(MedailleRequestDTO dto) {
        Athlete athlete = athleteRepository.findById(dto.getAthleteId())
                .orElseThrow(() -> new ResourceNotFoundException("Athlète non trouvé avec l'id: " + dto.getAthleteId()));
        Pays pays = paysRepository.findById(dto.getPaysId())
                .orElseThrow(() -> new ResourceNotFoundException("Pays non trouvé avec l'id: " + dto.getPaysId()));
        Competition competition = competitionRepository.findById(dto.getCompetitionId())
                .orElseThrow(() -> new ResourceNotFoundException("Compétition non trouvée avec l'id: " + dto.getCompetitionId()));

        Medaille medaille = new Medaille();
        medaille.setType(dto.getType());
        medaille.setDateObtention(dto.getDateObtention());
        medaille.setAthlete(athlete);
        medaille.setPays(pays);
        medaille.setCompetition(competition);

        return toResponseDTO(medailleRepository.save(medaille));
    }

    public MedailleResponseDTO update(Long id, MedailleRequestDTO dto) {
        Medaille medaille = findOrThrow(id);

        Athlete athlete = athleteRepository.findById(dto.getAthleteId())
                .orElseThrow(() -> new ResourceNotFoundException("Athlète non trouvé avec l'id: " + dto.getAthleteId()));
        Pays pays = paysRepository.findById(dto.getPaysId())
                .orElseThrow(() -> new ResourceNotFoundException("Pays non trouvé avec l'id: " + dto.getPaysId()));
        Competition competition = competitionRepository.findById(dto.getCompetitionId())
                .orElseThrow(() -> new ResourceNotFoundException("Compétition non trouvée avec l'id: " + dto.getCompetitionId()));

        medaille.setType(dto.getType());
        medaille.setDateObtention(dto.getDateObtention());
        medaille.setAthlete(athlete);
        medaille.setPays(pays);
        medaille.setCompetition(competition);

        return toResponseDTO(medailleRepository.save(medaille));
    }

    public void delete(Long id) {
        if (!medailleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Médaille non trouvée avec l'id: " + id);
        }
        medailleRepository.deleteById(id);
    }

    public List<ClassementResponseDTO> getClassement(String tri) {
        List<Medaille> toutes = medailleRepository.findAll();

        // Grouper par pays
        Map<Pays, Map<TypeMedaille, Long>> grouped = toutes.stream()
                .collect(Collectors.groupingBy(
                        Medaille::getPays,
                        Collectors.groupingBy(Medaille::getType, Collectors.counting())
                ));

        List<ClassementResponseDTO> classement = grouped.entrySet().stream()
                .map(e -> {
                    Pays p = e.getKey();
                    Map<TypeMedaille, Long> m = e.getValue();
                    long or     = m.getOrDefault(TypeMedaille.OR,     0L);
                    long argent = m.getOrDefault(TypeMedaille.ARGENT, 0L);
                    long bronze = m.getOrDefault(TypeMedaille.BRONZE, 0L);
                    long total  = or + argent + bronze;
                    long points = (or * 3) + (argent * 2) + (bronze);
                    return new ClassementResponseDTO(
                            p.getNom(), p.getCode(), p.getDrapeau(),
                            or, argent, bronze, total, points
                    );
                })
                .collect(Collectors.toList());

        // Tri
        TriStrategy strategy = triStrategies.getOrDefault(tri, triStrategies.get("total"));
        classement.sort(strategy.comparator());
        
        return classement;
    }

    public ClassementResponseDTO getStatsByPays(Long paysId) {
        Pays pays = paysRepository.findById(paysId)
                .orElseThrow(() -> new ResourceNotFoundException("Pays non trouvé avec l'id: " + paysId));

        List<Medaille> medailles = medailleRepository.findByPaysId(paysId);

        long or     = medailles.stream().filter(m -> m.getType() == TypeMedaille.OR).count();
        long argent = medailles.stream().filter(m -> m.getType() == TypeMedaille.ARGENT).count();
        long bronze = medailles.stream().filter(m -> m.getType() == TypeMedaille.BRONZE).count();

        return new ClassementResponseDTO(
                pays.getNom(), pays.getCode(), pays.getDrapeau(),
                or, argent, bronze,
                or + argent + bronze,
                (or * 3) + (argent * 2) + bronze
        );
    }

    // Helpers
    private Medaille findOrThrow(Long id) {
        return medailleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Médaille non trouvée avec l'id: " + id));
    }

    private MedailleResponseDTO toResponseDTO(Medaille m) {
        return new MedailleResponseDTO(
                m.getId(),
                m.getType(),
                m.getDateObtention(),
                m.getAthlete().getNom() + " " + m.getAthlete().getPrenom(),
                m.getAthlete().getPrenom(),
                m.getPays().getNom(),
                m.getCompetition().getNom()
        );
    }
}