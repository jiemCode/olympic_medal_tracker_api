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

/**
 * Service métier pour gérer les médailles : CRUD, filtrage par athlète/compétition,
 * et calcul des classements par pays avec stratégies de tri configurables.
 */
@Service
@RequiredArgsConstructor
public class MedailleService implements ReadableService<MedailleResponseDTO>, WritableService<MedailleResponseDTO, MedailleRequestDTO>, Classifiable, MedailleFilterable{

    private final MedailleRepository medailleRepository;
    private final AthleteRepository athleteRepository;
    private final PaysRepository paysRepository;
    private final CompetitionRepository competitionRepository;
    private final Map<String, TriStrategy> triStrategies;  // Injection des méthodes de tri

    /**
     * Retourne une page de médailles avec tri appliqué.
     *
     * @param pageable configuration pagination/tri.
     * @return page de médailles.
     */
    public PageResponseDTO<MedailleResponseDTO> getAll(Pageable pageable) {
        return PageResponseDTO.from(
            medailleRepository.findAll(pageable).map(this::toResponseDTO)
        );
    }

    /**
     * Détail d'une médaille par identifiant.
     *
     * @param id identifiant de la médaille.
     * @return médaille trouvée.
     */
    public MedailleResponseDTO getById(Long id) {
        return toResponseDTO(findOrThrow(id));
    }

    /**
     * Médailles attribuées à un athlète.
     *
     * @param athleteId identifiant de l'athlète.
     * @return liste des médailles de l'athlète.
     */
    public List<MedailleResponseDTO> getByAthlete(Long athleteId) {
        if (!athleteRepository.existsById(athleteId)) {
            throw new ResourceNotFoundException("Athlète non trouvé avec l'id: " + athleteId);
        }
        return medailleRepository.findByAthleteId(athleteId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Médailles associées à une compétition.
     *
     * @param competitionId identifiant de la compétition.
     * @return liste des médailles attribuées dans cette compétition.
     */
    public List<MedailleResponseDTO> getByCompetition(Long competitionId) {
        if (!competitionRepository.existsById(competitionId)) {
            throw new ResourceNotFoundException("Compétition non trouvée avec l'id: " + competitionId);
        }
        return medailleRepository.findByCompetitionId(competitionId).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Attribue une nouvelle médaille en validant l'existence des entités liées.
     *
     * @param dto payload validé (type, date, athlète, pays, compétition).
     * @return médaille créée.
     */
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

    /**
     * Met à jour une médaille existante.
     *
     * @param id identifiant de la médaille.
     * @param dto nouvelles valeurs.
     * @return médaille mise à jour.
     */
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

    /**
     * Supprime une médaille par identifiant.
     *
     * @param id identifiant de la médaille.
     */
    public void delete(Long id) {
        if (!medailleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Médaille non trouvée avec l'id: " + id);
        }
        medailleRepository.deleteById(id);
    }

    /**
     * Calcule le classement des pays selon une stratégie de tri.
     *
     * @param tri clé de stratégie : {@code or}, {@code argent}, {@code bronze},
     *            {@code points} (3/2/1) ou {@code total} (défaut).
     * @return classement ordonné des pays.
     */
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
                            p.getId(), p.getNom(), p.getCode(), p.getDrapeau(),
                            or, argent, bronze, total, points
                    );
                })
                .collect(Collectors.toList());

        // Tri
        TriStrategy strategy = triStrategies.getOrDefault(tri, triStrategies.get("total"));
        classement.sort(strategy.comparator());
        
        return classement;
    }

    /**
     * Statistiques de médailles pour un pays.
     *
     * @param paysId identifiant du pays.
     * @return résumé des médailles et des points.
     */
    public ClassementResponseDTO getStatsByPays(Long paysId) {
        Pays pays = paysRepository.findById(paysId)
                .orElseThrow(() -> new ResourceNotFoundException("Pays non trouvé avec l'id: " + paysId));

        List<Medaille> medailles = medailleRepository.findByPaysId(paysId);

        long or     = medailles.stream().filter(m -> m.getType() == TypeMedaille.OR).count();
        long argent = medailles.stream().filter(m -> m.getType() == TypeMedaille.ARGENT).count();
        long bronze = medailles.stream().filter(m -> m.getType() == TypeMedaille.BRONZE).count();

        return new ClassementResponseDTO(
                pays.getId(), pays.getNom(), pays.getCode(), pays.getDrapeau(),
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
                m.getAthlete().getId(),
                m.getAthlete().getNom() + " " + m.getAthlete().getPrenom(),
                m.getAthlete().getPrenom(),
                m.getPays().getId(),
                m.getPays().getNom(),
                m.getCompetition().getId(),
                m.getCompetition().getNom()
        );
    }
}
