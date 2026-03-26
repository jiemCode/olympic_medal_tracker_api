package com.fleety.olympics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fleety.olympics.dto.response.ClassementResponseDTO;
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

            TriStrategy triParOr = () -> java.util.Comparator.comparingLong(ClassementResponseDTO::getOr).reversed();
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

            TriStrategy triTotal = () -> java.util.Comparator.comparingLong(ClassementResponseDTO::getTotal).reversed();
            when(triStrategies.getOrDefault(any(), any())).thenReturn(triTotal);
            when(triStrategies.get("total")).thenReturn(triTotal);

            List<ClassementResponseDTO> result = medailleService.getClassement(null);

            assertThat(result.get(0).getPoints()).isEqualTo(3L);
            assertThat(result.get(0).getTotal()).isEqualTo(1L);
        }

        @Test
        @DisplayName("doit retourner une liste vide si aucune médaille")
        void shouldReturnEmpty_whenNoMedailles() {

            when(medailleRepository.findAll()).thenReturn(List.of());
            TriStrategy triTotal = () -> java.util.Comparator.comparingLong(ClassementResponseDTO::getTotal);
            when(triStrategies.get("total")).thenReturn(triTotal);
            when(triStrategies.getOrDefault(any(), any())).thenReturn(triTotal);

            List<ClassementResponseDTO> result = medailleService.getClassement(null);

            assertThat(result).isEmpty();
        }
    }

    // ── Helper ───────────────────────────────────────────
    private Medaille buildMedaille(Long id, TypeMedaille type, Pays pays, Athlete athlete) {
        return Medaille.builder()
                .id(id).type(type)
                .dateObtention(LocalDate.of(2026, 8, 5))
                .athlete(athlete).pays(pays).competition(competition)
                .build();
    }
}
