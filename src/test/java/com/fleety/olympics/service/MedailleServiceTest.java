package com.fleety.olympics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

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
import com.fleety.olympics.strategy.TriStrategy;


@ExtendWith(MockitoExtension.class)
@DisplayName("MedailleService — Tests Unitaires")
@ActiveProfiles("test")
class MedailleServiceTest {

    @Mock private MedailleRepository medailleRepository;
    @Mock private AthleteRepository athleteRepository;
    @Mock private PaysRepository paysRepository;
    @Mock private CompetitionRepository competitionRepository;
    @Mock private Map<String, TriStrategy> triStrategies;

    @InjectMocks
    private MedailleService medailleService;

    private Pays senegal;
    private Pays usa;
    private Athlete faye;
    private Competition competition;
    private TriStrategy triParOr;
    private TriStrategy triTotal;

    @BeforeEach
    void setUp() {
        senegal = Pays.builder().id(1L).nom("Sénégal").code("SEN").drapeau("🇸🇳").build();
        usa     = Pays.builder().id(2L).nom("USA").code("USA").drapeau("🇺🇸").build();

        faye = Athlete.builder()
                .id(1L).nom("Faye").prenom("Mbaye")
                .dateNaissance(LocalDate.of(1998, 6, 14))
                .discipline("Lutte").pays(senegal)
                .build();

        competition = Competition.builder()
                .id(1L).nom("Lutte 65kg").discipline("Lutte")
                .dateDebut(LocalDate.of(2026, 8, 5))
                .dateFin(LocalDate.of(2026, 8, 5))
                .statut(Competition.StatusCompetition.TERMINEE)
                .build();

        triParOr    = () -> java.util.Comparator.comparingLong(ClassementResponseDTO::getOr).reversed();
        triTotal    = () -> java.util.Comparator.comparingLong(ClassementResponseDTO::getTotal).reversed();
    }

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("doit retourner tous les médailles")
        void shouldReturnAllMedailles() {

            Medaille m = buildMedaille(1L, TypeMedaille.OR, senegal, faye);
            Page<Medaille> page = new PageImpl<>(List.of(m));

            when(medailleRepository.findAll(any(Pageable.class)))
                    .thenReturn(page);

            PageResponseDTO<MedailleResponseDTO> result =
                    medailleService.getAll(PageRequest.of(0, 10));

            assertThat(result.getContenu()).hasSize(1);
            assertThat(result.getContenu().get(0).getId()).isEqualTo(1L);
            assertThat(result.getContenu().get(0).getType()).isEqualTo(TypeMedaille.OR);
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(medailleRepository, times(1)).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("doit retourner une liste vide si aucune médaille")
        void shouldReturnEmpty_whenNone() {

            Page<Medaille> page = new PageImpl<>(List.of());

            when(medailleRepository.findAll(any(Pageable.class)))
                    .thenReturn(page);

            PageResponseDTO<MedailleResponseDTO> result =
                    medailleService.getAll(PageRequest.of(0, 10));

            assertThat(result.getContenu()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("doit retourner la médaille correspondante")
        void shouldReturnMedaille_whenFound() {
            Medaille m = buildMedaille(1L, TypeMedaille.OR, senegal, faye);
            when(medailleRepository.findById(1L)).thenReturn(Optional.of(m));

            MedailleResponseDTO result = medailleService.getById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getType()).isEqualTo(TypeMedaille.OR);
        }

        @Test
        @DisplayName("doit lever ResourceNotFoundException si introuvable")
        void shouldThrow_whenNotFound() {
            when(medailleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> medailleService.getById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("getByAthlete()")
    class GetByAthlete {

        @Test
        @DisplayName("doit retourner les médailles de l'athlète")
        void shouldReturnMedailles_forAthlete() {
            Medaille m = buildMedaille(1L, TypeMedaille.OR, senegal, faye);
            when(athleteRepository.existsById(1L)).thenReturn(true);
            when(medailleRepository.findByAthleteId(1L)).thenReturn(List.of(m));

            List<MedailleResponseDTO> result = medailleService.getByAthlete(1L);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("doit lever ResourceNotFoundException si athlète inexistant")
        void shouldThrow_whenAthleteNotFound() {
            when(athleteRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> medailleService.getByAthlete(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("getByCompetition()")
    class GetByCompetition {

        @Test
        @DisplayName("doit retourner les médailles de la compétition")
        void shouldReturnMedailles_forCompetition() {
            Medaille m = buildMedaille(1L, TypeMedaille.ARGENT, usa, faye);
            when(competitionRepository.existsById(1L)).thenReturn(true);
            when(medailleRepository.findByCompetitionId(1L)).thenReturn(List.of(m));

            List<MedailleResponseDTO> result = medailleService.getByCompetition(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getType()).isEqualTo(TypeMedaille.ARGENT);
        }

        @Test
        @DisplayName("doit lever ResourceNotFoundException si compétition inexistante")
        void shouldThrow_whenCompetitionNotFound() {
            when(competitionRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> medailleService.getByCompetition(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("doit créer et retourner la médaille")
        void shouldCreateMedaille() {
            MedailleRequestDTO dto = buildRequestDTO(TypeMedaille.OR, 1L, 1L, 1L);
            Medaille saved = buildMedaille(10L, TypeMedaille.OR, senegal, faye);

            when(athleteRepository.findById(1L)).thenReturn(Optional.of(faye));
            when(paysRepository.findById(1L)).thenReturn(Optional.of(senegal));
            when(competitionRepository.findById(1L)).thenReturn(Optional.of(competition));
            when(medailleRepository.save(any())).thenReturn(saved);

            MedailleResponseDTO result = medailleService.create(dto);

            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getType()).isEqualTo(TypeMedaille.OR);
            verify(medailleRepository).save(any(Medaille.class));
        }

        @Test
        @DisplayName("doit lever ResourceNotFoundException si athlète introuvable")
        void shouldThrow_whenAthleteNotFound() {
            MedailleRequestDTO dto = buildRequestDTO(TypeMedaille.OR, 99L, 1L, 1L);
            when(athleteRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> medailleService.create(dto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("doit lever ResourceNotFoundException si pays introuvable")
        void shouldThrow_whenPaysNotFound() {
            MedailleRequestDTO dto = buildRequestDTO(TypeMedaille.OR, 1L, 99L, 1L);
            when(athleteRepository.findById(1L)).thenReturn(Optional.of(faye));
            when(paysRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> medailleService.create(dto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("doit lever ResourceNotFoundException si compétition introuvable")
        void shouldThrow_whenCompetitionNotFound() {
            MedailleRequestDTO dto = buildRequestDTO(TypeMedaille.OR, 1L, 1L, 99L);
            when(athleteRepository.findById(1L)).thenReturn(Optional.of(faye));
            when(paysRepository.findById(1L)).thenReturn(Optional.of(senegal));
            when(competitionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> medailleService.create(dto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("doit mettre à jour et retourner la médaille")
        void shouldUpdateMedaille() {
            MedailleRequestDTO dto = buildRequestDTO(TypeMedaille.BRONZE, 1L, 1L, 1L);
            Medaille existing = buildMedaille(1L, TypeMedaille.OR, senegal, faye);
            Medaille updated  = buildMedaille(1L, TypeMedaille.BRONZE, senegal, faye);

            when(medailleRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(athleteRepository.findById(1L)).thenReturn(Optional.of(faye));
            when(paysRepository.findById(1L)).thenReturn(Optional.of(senegal));
            when(competitionRepository.findById(1L)).thenReturn(Optional.of(competition));
            when(medailleRepository.save(any())).thenReturn(updated);

            MedailleResponseDTO result = medailleService.update(1L, dto);

            assertThat(result.getType()).isEqualTo(TypeMedaille.BRONZE);
            verify(medailleRepository).save(existing);
        }

        @Test
        @DisplayName("doit lever ResourceNotFoundException si médaille introuvable")
        void shouldThrow_whenMedailleNotFound() {
            MedailleRequestDTO dto = buildRequestDTO(TypeMedaille.OR, 1L, 1L, 1L);
            when(medailleRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> medailleService.update(99L, dto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("doit lever ResourceNotFoundException si athlète introuvable")
        void shouldThrow_whenAthleteNotFound() {
            MedailleRequestDTO dto = buildRequestDTO(TypeMedaille.OR, 99L, 1L, 1L);
            Medaille existing = buildMedaille(1L, TypeMedaille.OR, senegal, faye);
            when(medailleRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(athleteRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> medailleService.update(1L, dto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("doit lever ResourceNotFoundException si pays introuvable")
        void shouldThrow_whenPaysNotFound() {
            MedailleRequestDTO dto = buildRequestDTO(TypeMedaille.OR, 1L, 99L, 1L);
            Medaille existing = buildMedaille(1L, TypeMedaille.OR, senegal, faye);
            when(medailleRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(athleteRepository.findById(1L)).thenReturn(Optional.of(faye));
            when(paysRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> medailleService.update(1L, dto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("doit lever ResourceNotFoundException si compétition introuvable")
        void shouldThrow_whenCompetitionNotFound() {
            MedailleRequestDTO dto = buildRequestDTO(TypeMedaille.OR, 1L, 1L, 99L);
            Medaille existing = buildMedaille(1L, TypeMedaille.OR, senegal, faye);
            when(medailleRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(athleteRepository.findById(1L)).thenReturn(Optional.of(faye));
            when(paysRepository.findById(1L)).thenReturn(Optional.of(senegal));
            when(competitionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> medailleService.update(1L, dto))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("doit supprimer la médaille existante")
        void shouldDelete_whenExists() {
            when(medailleRepository.existsById(1L)).thenReturn(true);

            medailleService.delete(1L);

            verify(medailleRepository).deleteById(1L);
        }

        @Test
        @DisplayName("doit lever ResourceNotFoundException si médaille introuvable")
        void shouldThrow_whenNotFound() {
            when(medailleRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> medailleService.delete(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(medailleRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("getClassement()")
    class GetClassement {

        @Test
        @DisplayName("doit retourner le classement trié par or")
        void shouldReturnClassement_sortedByOr() {
            Medaille orSen    = buildMedaille(1L, TypeMedaille.OR,     senegal, faye);
            Medaille argentUs = buildMedaille(2L, TypeMedaille.ARGENT, usa,     faye);
            Medaille bronzeUs = buildMedaille(3L, TypeMedaille.BRONZE, usa,     faye);

            when(medailleRepository.findAll()).thenReturn(List.of(orSen, argentUs, bronzeUs));
            when(triStrategies.getOrDefault(eq("or"), any())).thenReturn(triParOr);
            when(triStrategies.get("total")).thenReturn(triParOr);

            List<ClassementResponseDTO> result = medailleService.getClassement("or");

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getPaysCode()).isEqualTo("SEN");
            assertThat(result.get(0).getOr()).isEqualTo(1L);
            assertThat(result.get(1).getPaysCode()).isEqualTo("USA");
            assertThat(result.get(1).getArgent()).isEqualTo(1L);
            assertThat(result.get(1).getBronze()).isEqualTo(1L);
        }

        @Test
        @DisplayName("doit calculer les points correctement (or=3, argent=2, bronze=1)")
        void shouldCalculatePoints_correctly() {
            Medaille orSen = buildMedaille(1L, TypeMedaille.OR, senegal, faye);
            when(medailleRepository.findAll()).thenReturn(List.of(orSen));
            when(triStrategies.getOrDefault(any(), any())).thenReturn(triTotal);
            when(triStrategies.get("total")).thenReturn(triTotal);

            List<ClassementResponseDTO> result = medailleService.getClassement(null);

            assertThat(result.get(0).getPoints()).isEqualTo(3L);
            assertThat(result.get(0).getTotal()).isEqualTo(1L);
        }

        @Test
        @DisplayName("doit calculer les points argent (argent=2 pts)")
        void shouldCalculatePoints_forArgent() {
            Medaille argentSen = buildMedaille(2L, TypeMedaille.ARGENT, senegal, faye);
            when(medailleRepository.findAll()).thenReturn(List.of(argentSen));
            when(triStrategies.getOrDefault(any(), any())).thenReturn(triTotal);
            when(triStrategies.get("total")).thenReturn(triTotal);

            List<ClassementResponseDTO> result = medailleService.getClassement(null);

            assertThat(result.get(0).getArgent()).isEqualTo(1L);
            assertThat(result.get(0).getPoints()).isEqualTo(2L);
        }

        @Test
        @DisplayName("doit calculer les points bronze (bronze=1 pt)")
        void shouldCalculatePoints_forBronze() {
            Medaille bronzeSen = buildMedaille(3L, TypeMedaille.BRONZE, senegal, faye);
            when(medailleRepository.findAll()).thenReturn(List.of(bronzeSen));
            when(triStrategies.getOrDefault(any(), any())).thenReturn(triTotal);
            when(triStrategies.get("total")).thenReturn(triTotal);

            List<ClassementResponseDTO> result = medailleService.getClassement(null);

            assertThat(result.get(0).getBronze()).isEqualTo(1L);
            assertThat(result.get(0).getPoints()).isEqualTo(1L);
        }

        @Test
        @DisplayName("doit retourner une liste vide si aucune médaille")
        void shouldReturnEmpty_whenNoMedailles() {
            when(medailleRepository.findAll()).thenReturn(List.of());
            when(triStrategies.get("total")).thenReturn(triTotal);
            when(triStrategies.getOrDefault(any(), any())).thenReturn(triTotal);

            List<ClassementResponseDTO> result = medailleService.getClassement(null);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("doit utiliser la stratégie 'total' si tri null")
        void shouldFallbackToTotal_whenTriIsNull() {
            Medaille m = buildMedaille(1L, TypeMedaille.OR, senegal, faye);
            when(medailleRepository.findAll()).thenReturn(List.of(m));
            when(triStrategies.getOrDefault(isNull(), any())).thenReturn(triTotal);
            when(triStrategies.get("total")).thenReturn(triTotal);

            List<ClassementResponseDTO> result = medailleService.getClassement(null);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("doit agréger correctement plusieurs médailles du même pays")
        void shouldAggregate_multipleTypesForSamePays() {
            Medaille or1    = buildMedaille(1L, TypeMedaille.OR,     senegal, faye);
            Medaille or2    = buildMedaille(2L, TypeMedaille.OR,     senegal, faye);
            Medaille argent = buildMedaille(3L, TypeMedaille.ARGENT, senegal, faye);
            Medaille bronze = buildMedaille(4L, TypeMedaille.BRONZE, senegal, faye);

            when(medailleRepository.findAll()).thenReturn(List.of(or1, or2, argent, bronze));
            when(triStrategies.getOrDefault(any(), any())).thenReturn(triTotal);
            when(triStrategies.get("total")).thenReturn(triTotal);

            List<ClassementResponseDTO> result = medailleService.getClassement("total");

            assertThat(result).hasSize(1);
            ClassementResponseDTO dto = result.get(0);
            assertThat(dto.getOr()).isEqualTo(2L);
            assertThat(dto.getArgent()).isEqualTo(1L);
            assertThat(dto.getBronze()).isEqualTo(1L);
            assertThat(dto.getTotal()).isEqualTo(4L);
            // points = (2*3) + (1*2) + (1*1) = 9
            assertThat(dto.getPoints()).isEqualTo(9L);
        }
    }

    @Nested
    @DisplayName("getStatsByPays()")
    class GetStatsByPays {

        @Test
        @DisplayName("doit retourner les stats du pays")
        void shouldReturnStats_forPays() {
            Medaille or     = buildMedaille(1L, TypeMedaille.OR,     senegal, faye);
            Medaille argent = buildMedaille(2L, TypeMedaille.ARGENT, senegal, faye);
            Medaille bronze = buildMedaille(3L, TypeMedaille.BRONZE, senegal, faye);

            when(paysRepository.findById(1L)).thenReturn(Optional.of(senegal));
            when(medailleRepository.findByPaysId(1L)).thenReturn(List.of(or, argent, bronze));

            ClassementResponseDTO result = medailleService.getStatsByPays(1L);

            assertThat(result.getPaysNom()).isEqualTo("Sénégal");
            assertThat(result.getPaysCode()).isEqualTo("SEN");
            assertThat(result.getOr()).isEqualTo(1L);
            assertThat(result.getArgent()).isEqualTo(1L);
            assertThat(result.getBronze()).isEqualTo(1L);
            assertThat(result.getTotal()).isEqualTo(3L);
            // points = 3 + 2 + 1 = 6
            assertThat(result.getPoints()).isEqualTo(6L);
        }

        @Test
        @DisplayName("doit retourner des zéros si aucune médaille pour ce pays")
        void shouldReturnZeros_whenNoPaysFound() {
            when(paysRepository.findById(1L)).thenReturn(Optional.of(senegal));
            when(medailleRepository.findByPaysId(1L)).thenReturn(Collections.emptyList());

            ClassementResponseDTO result = medailleService.getStatsByPays(1L);

            assertThat(result.getOr()).isEqualTo(0L);
            assertThat(result.getArgent()).isEqualTo(0L);
            assertThat(result.getBronze()).isEqualTo(0L);
            assertThat(result.getTotal()).isEqualTo(0L);
            assertThat(result.getPoints()).isEqualTo(0L);
        }

        @Test
        @DisplayName("doit lever ResourceNotFoundException si pays introuvable")
        void shouldThrow_whenPaysNotFound() {
            when(paysRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> medailleService.getStatsByPays(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Medaille buildMedaille(Long id, TypeMedaille type, Pays pays, Athlete athlete) {
        return Medaille.builder()
                .id(id).type(type)
                .dateObtention(LocalDate.of(2026, 8, 5))
                .athlete(athlete).pays(pays).competition(competition)
                .build();
    }

    private MedailleRequestDTO buildRequestDTO(TypeMedaille type,
                                               Long athleteId,
                                               Long paysId,
                                               Long competitionId) {
        MedailleRequestDTO dto = new MedailleRequestDTO();
        dto.setType(type);
        dto.setDateObtention(LocalDate.of(2026, 8, 5));
        dto.setAthleteId(athleteId);
        dto.setPaysId(paysId);
        dto.setCompetitionId(competitionId);
        return dto;
    }
}