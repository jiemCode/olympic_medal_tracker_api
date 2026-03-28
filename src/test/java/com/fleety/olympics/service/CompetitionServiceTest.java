package com.fleety.olympics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import com.fleety.olympics.dto.request.CompetitionRequestDTO;
import com.fleety.olympics.dto.response.CompetitionResponseDTO;
import com.fleety.olympics.dto.response.PageResponseDTO;
import com.fleety.olympics.exception.DuplicateResourceException;
import com.fleety.olympics.exception.ResourceNotFoundException;
import com.fleety.olympics.model.Competition;
import com.fleety.olympics.model.Competition.StatusCompetition;
import com.fleety.olympics.repository.CompetitionRepository;


@ExtendWith(MockitoExtension.class)
@DisplayName("CompetitionService — Tests Unitaires")
@ActiveProfiles("test")
class CompetitionServiceTest {

    @Mock
    private CompetitionRepository competitionRepository;

    @InjectMocks
    private CompetitionService competitionService;

    private Competition competition;
    private CompetitionRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        competition = Competition.builder()
                .id(1L)
                .nom("100m Hommes")
                .discipline("Athlétisme")
                .dateDebut(LocalDate.of(2026, 7, 26))
                .dateFin(LocalDate.of(2026, 7, 26))
                .statut(StatusCompetition.PLANIFIEE)
                .build();

        requestDTO = new CompetitionRequestDTO();
        requestDTO.setNom("100m Hommes");
        requestDTO.setDiscipline("Athlétisme");
        requestDTO.setDateDebut(LocalDate.of(2026, 7, 26));
        requestDTO.setDateFin(LocalDate.of(2026, 7, 26));
        requestDTO.setStatut(StatusCompetition.PLANIFIEE);
    }

    // ─── getAll ───────────────────────────────────────────
    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("doit retourner toutes les compétitions")
        void shouldReturnAllCompetitions() {

            Competition c2 = Competition.builder()
                    .id(2L).nom("50m Nage Libre").discipline("Natation")
                    .dateDebut(LocalDate.of(2026, 7, 28))
                    .dateFin(LocalDate.of(2026, 7, 28))
                    .statut(StatusCompetition.EN_COURS)
                    .build();
            Page<Competition> page = new PageImpl<>(List.of(competition, c2));

            when(competitionRepository.findAll(any(Pageable.class)))
                    .thenReturn(page);

            PageResponseDTO<CompetitionResponseDTO> result =
                    competitionService.getAll(PageRequest.of(0, 10));


            assertThat(result.getContenu()).hasSize(2);
            assertThat(result.getContenu().get(0).getNom()).isEqualTo("100m Hommes");
            assertThat(result.getContenu().get(1).getNom()).isEqualTo("50m Nage Libre");
            assertThat(result.getTotalElements()).isEqualTo(2);
            verify(competitionRepository, times(1)).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("doit retourner une liste vide si aucune compétition")
        void shouldReturnEmptyList() {

            Page<Competition> page = new PageImpl<>(List.of());

            when(competitionRepository.findAll(any(Pageable.class)))
                    .thenReturn(page);

            PageResponseDTO<CompetitionResponseDTO> result =
                    competitionService.getAll(PageRequest.of(0, 10));

            assertThat(result.getContenu()).isEmpty();
            verify(competitionRepository, times(1)).findAll(any(Pageable.class));
        }
    }

    // ─── getById ──────────────────────────────────────────
    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("doit retourner la compétition quand elle existe")
        void shouldReturnCompetition_whenExists() {

            when(competitionRepository.findById(1L)).thenReturn(Optional.of(competition));

            CompetitionResponseDTO result = competitionService.getById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getNom()).isEqualTo("100m Hommes");
            assertThat(result.getDiscipline()).isEqualTo("Athlétisme");
            assertThat(result.getStatut()).isEqualTo(StatusCompetition.PLANIFIEE);
        }

        @Test
        @DisplayName("doit lever ResourceNotFoundException quand la compétition n'existe pas")
        void shouldThrowException_whenNotFound() {

            when(competitionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> competitionService.getById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ─── create ───────────────────────────────────────────
    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("doit créer la compétition quand les données sont valides")
        void shouldCreateCompetition_whenValid() {

            when(competitionRepository.existsByNom("100m Hommes")).thenReturn(false);
            when(competitionRepository.save(any(Competition.class))).thenReturn(competition);

            CompetitionResponseDTO result = competitionService.create(requestDTO);

            assertThat(result.getNom()).isEqualTo("100m Hommes");
            assertThat(result.getDiscipline()).isEqualTo("Athlétisme");
            assertThat(result.getStatut()).isEqualTo(StatusCompetition.PLANIFIEE);
            verify(competitionRepository, times(1)).save(any(Competition.class));
        }

        @Test
        @DisplayName("doit lever DuplicateResourceException si le nom existe déjà")
        void shouldThrowException_whenNomAlreadyExists() {

            when(competitionRepository.existsByNom("100m Hommes")).thenReturn(true);

            assertThatThrownBy(() -> competitionService.create(requestDTO))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("100m Hommes");

            verify(competitionRepository, never()).save(any());
        }
    }

    // ─── update ───────────────────────────────────────────
    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("doit mettre à jour le statut de la compétition")
        void shouldUpdateStatut() {

            when(competitionRepository.findById(1L)).thenReturn(Optional.of(competition));
            when(competitionRepository.save(any(Competition.class))).thenReturn(competition);

            requestDTO.setStatut(StatusCompetition.EN_COURS);

            competitionService.update(1L, requestDTO);

            verify(competitionRepository, times(1)).save(any(Competition.class));
            assertThat(competition.getStatut()).isEqualTo(StatusCompetition.EN_COURS);
        }

        @Test
        @DisplayName("doit mettre à jour tous les champs")
        void shouldUpdateAllFields() {

            when(competitionRepository.findById(1L)).thenReturn(Optional.of(competition));
            when(competitionRepository.save(any(Competition.class))).thenReturn(competition);

            requestDTO.setNom("100m Hommes — Finale");
            requestDTO.setDiscipline("Athlétisme");
            requestDTO.setStatut(StatusCompetition.TERMINEE);

            competitionService.update(1L, requestDTO);

            assertThat(competition.getNom()).isEqualTo("100m Hommes — Finale");
            assertThat(competition.getStatut()).isEqualTo(StatusCompetition.TERMINEE);
            verify(competitionRepository, times(1)).save(competition);
        }

        @Test
        @DisplayName("doit lever ResourceNotFoundException quand la compétition n'existe pas")
        void shouldThrowException_whenNotFound() {

            when(competitionRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> competitionService.update(99L, requestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(competitionRepository, never()).save(any());
        }
    }

    // ─── delete ───────────────────────────────────────────
    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("doit supprimer la compétition quand elle existe")
        void shouldDelete_whenExists() {

            when(competitionRepository.existsById(1L)).thenReturn(true);

            competitionService.delete(1L);

            verify(competitionRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("doit lever ResourceNotFoundException quand la compétition n'existe pas")
        void shouldThrowException_whenNotFound() {

            when(competitionRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> competitionService.delete(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(competitionRepository, never()).deleteById(any());
        }
    }
}