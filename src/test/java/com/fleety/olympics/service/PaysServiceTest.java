package com.fleety.olympics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import com.fleety.olympics.dto.request.PaysRequestDTO;
import com.fleety.olympics.dto.response.PageResponseDTO;
import com.fleety.olympics.dto.response.PaysResponseDTO;
import com.fleety.olympics.exception.DuplicateResourceException;
import com.fleety.olympics.exception.ResourceNotFoundException;
import com.fleety.olympics.model.Pays;
import com.fleety.olympics.repository.PaysRepository;


@ExtendWith(MockitoExtension.class)
@DisplayName("PaysService — Tests Unitaires")
@ActiveProfiles("test")
class PaysServiceTest {

    @Mock
    private PaysRepository paysRepository;

    @InjectMocks
    private PaysService paysService;

    private Pays pays;
    private PaysRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        pays = Pays.builder()
                .id(1L)
                .nom("Sénégal")
                .code("SEN")
                .drapeau("🇸🇳")
                .build();

        requestDTO = new PaysRequestDTO();
        requestDTO.setNom("Sénégal");
        requestDTO.setCode("SEN");
        requestDTO.setDrapeau("🇸🇳");
    }

    // getAll
    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("doit retourner la liste de tous les pays")
        void shouldReturnAllPays() {
            Pays pays2 = Pays.builder().id(2L).nom("France").code("FRA").drapeau("🇫🇷").build();
            Page<Pays> page = new PageImpl<>(List.of(pays, pays2));

            when(paysRepository.findAll(any(Pageable.class)))
                    .thenReturn(page);

            PageResponseDTO<PaysResponseDTO> result =
                    paysService.getAll(PageRequest.of(0, 10));

            assertThat(result.getContenu()).hasSize(2);
            assertThat(result.getContenu().get(0).getNom()).isEqualTo("Sénégal");
            assertThat(result.getContenu().get(1).getNom()).isEqualTo("France");
            assertThat(result.getTotalElements()).isEqualTo(2);
            verify(paysRepository, times(1)).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("doit retourner une liste vide si aucun pays")
        void shouldReturnEmptyList() {

            Page<Pays> page = new PageImpl<>(List.of());

            when(paysRepository.findAll(any(Pageable.class)))
                    .thenReturn(page);

            PageResponseDTO<PaysResponseDTO> result =
                    paysService.getAll(PageRequest.of(0, 10));

            assertThat(result.getContenu()).isEmpty();
        }
    }

    // getById
    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("doit retourner le pays quand il existe")
        void shouldReturnPays_whenExists() {

            when(paysRepository.findById(1L)).thenReturn(Optional.of(pays));

            PaysResponseDTO result = paysService.getById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getNom()).isEqualTo("Sénégal");
            assertThat(result.getCode()).isEqualTo("SEN");
            assertThat(result.getDrapeau()).isEqualTo("🇸🇳");
        }

        @Test
        @DisplayName("doit lever ResourceNotFoundException quand le pays n'existe pas")
        void shouldThrowException_whenNotFound() {

            when(paysRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paysService.getById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // create
    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("doit créer et retourner le pays quand les données sont valides")
        void shouldCreatePays_whenValid() {

            when(paysRepository.existsByCode("SEN")).thenReturn(false);
            when(paysRepository.existsByNom("Sénégal")).thenReturn(false);
            when(paysRepository.save(any(Pays.class))).thenReturn(pays);

            PaysResponseDTO result = paysService.create(requestDTO);

            assertThat(result.getNom()).isEqualTo("Sénégal");
            assertThat(result.getCode()).isEqualTo("SEN");
            verify(paysRepository, times(1)).save(any(Pays.class));
        }

        @Test
        @DisplayName("doit lever DuplicateResourceException si le code existe déjà")
        void shouldThrowException_whenCodeAlreadyExists() {

            when(paysRepository.existsByCode("SEN")).thenReturn(true);

            assertThatThrownBy(() -> paysService.create(requestDTO))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("SEN");

            verify(paysRepository, never()).save(any());
        }

        @Test
        @DisplayName("doit lever DuplicateResourceException si le nom existe déjà")
        void shouldThrowException_whenNomAlreadyExists() {

            when(paysRepository.existsByCode("SEN")).thenReturn(false);
            when(paysRepository.existsByNom("Sénégal")).thenReturn(true);

            assertThatThrownBy(() -> paysService.create(requestDTO))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Sénégal");

            verify(paysRepository, never()).save(any());
        }
    }

    // update
    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("doit mettre à jour le pays quand il existe")
        void shouldUpdatePays_whenExists() {

            when(paysRepository.findById(1L)).thenReturn(Optional.of(pays));
            when(paysRepository.save(any(Pays.class))).thenReturn(pays);

            requestDTO.setNom("Sénégal Updated");

            PaysResponseDTO result = paysService.update(1L, requestDTO);

            verify(paysRepository, times(1)).save(any(Pays.class));
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("doit lever ResourceNotFoundException quand le pays n'existe pas")
        void shouldThrowException_whenNotFound() {

            when(paysRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paysService.update(99L, requestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // delete
    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("doit supprimer le pays quand il existe")
        void shouldDelete_whenExists() {

            when(paysRepository.existsById(1L)).thenReturn(true);

            paysService.delete(1L);

            verify(paysRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("doit lever ResourceNotFoundException quand le pays n'existe pas")
        void shouldThrowException_whenNotFound() {

            when(paysRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> paysService.delete(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(paysRepository, never()).deleteById(any());
        }
    }
}