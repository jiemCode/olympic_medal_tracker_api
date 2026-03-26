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

import com.fleety.olympics.dto.request.AthleteRequestDTO;
import com.fleety.olympics.dto.response.AthleteResponseDTO;
import com.fleety.olympics.exception.ResourceNotFoundException;
import com.fleety.olympics.model.Athlete;
import com.fleety.olympics.model.Pays;
import com.fleety.olympics.repository.AthleteRepository;
import com.fleety.olympics.repository.PaysRepository;


@ExtendWith(MockitoExtension.class)
@DisplayName("AthleteService — Tests Unitaires")
class AthleteServiceTest {

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private PaysRepository paysRepository;

    @InjectMocks
    private AthleteService athleteService;

    private Pays pays;
    private Athlete athlete;
    private AthleteRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        pays = Pays.builder()
                .id(1L).nom("Sénégal").code("SEN").drapeau("🇸🇳")
                .build();

        athlete = Athlete.builder()
                .id(1L)
                .nom("Faye").prenom("Mbaye")
                .dateNaissance(LocalDate.of(1998, 6, 14))
                .discipline("Lutte")
                .pays(pays)
                .build();

        requestDTO = new AthleteRequestDTO();
        requestDTO.setNom("Faye");
        requestDTO.setPrenom("Mbaye");
        requestDTO.setDateNaissance(LocalDate.of(1998, 6, 14));
        requestDTO.setDiscipline("Lutte");
        requestDTO.setPaysId(1L);
    }

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("doit retourner tous les athlètes")
        void shouldReturnAllAthletes() {

            when(athleteRepository.findAll()).thenReturn(List.of(athlete));

            List<AthleteResponseDTO> result = athleteService.getAll();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getNom()).isEqualTo("Faye");
            assertThat(result.get(0).getPaysNom()).isEqualTo("Sénégal");
        }
    }

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("doit retourner l'athlète quand il existe")
        void shouldReturnAthlete_whenExists() {

            when(athleteRepository.findById(1L)).thenReturn(Optional.of(athlete));

            AthleteResponseDTO result = athleteService.getById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getNom()).isEqualTo("Faye");
            assertThat(result.getPrenom()).isEqualTo("Mbaye");
            assertThat(result.getDiscipline()).isEqualTo("Lutte");
        }

        @Test
        @DisplayName("doit lever ResourceNotFoundException quand l'athlète n'existe pas")
        void shouldThrowException_whenNotFound() {

            when(athleteRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> athleteService.getById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("getByPays()")
    class GetByPays {

        @Test
        @DisplayName("doit retourner les athlètes d'un pays")
        void shouldReturnAthletes_whenPaysExists() {

            when(paysRepository.existsById(1L)).thenReturn(true);
            when(athleteRepository.findByPaysId(1L)).thenReturn(List.of(athlete));

            List<AthleteResponseDTO> result = athleteService.getByPays(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPaysCode()).isEqualTo("SEN");
        }

        @Test
        @DisplayName("doit lever ResourceNotFoundException quand le pays n'existe pas")
        void shouldThrowException_whenPaysNotFound() {

            when(paysRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> athleteService.getByPays(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("doit créer un athlète quand le pays existe")
        void shouldCreateAthlete_whenPaysExists() {

            when(paysRepository.findById(1L)).thenReturn(Optional.of(pays));
            when(athleteRepository.save(any(Athlete.class))).thenReturn(athlete);

            AthleteResponseDTO result = athleteService.create(requestDTO);

            assertThat(result.getNom()).isEqualTo("Faye");
            assertThat(result.getPaysNom()).isEqualTo("Sénégal");
            verify(athleteRepository, times(1)).save(any(Athlete.class));
        }

        @Test
        @DisplayName("doit lever ResourceNotFoundException quand le pays n'existe pas")
        void shouldThrowException_whenPaysNotFound() {

            when(paysRepository.findById(99L)).thenReturn(Optional.empty());
            requestDTO.setPaysId(99L);

            assertThatThrownBy(() -> athleteService.create(requestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(athleteRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("doit supprimer l'athlète quand il existe")
        void shouldDelete_whenExists() {

            when(athleteRepository.existsById(1L)).thenReturn(true);

            athleteService.delete(1L);

            verify(athleteRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("doit lever ResourceNotFoundException quand l'athlète n'existe pas")
        void shouldThrowException_whenNotFound() {

            when(athleteRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> athleteService.delete(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(athleteRepository, never()).deleteById(any());
        }
    }
}
