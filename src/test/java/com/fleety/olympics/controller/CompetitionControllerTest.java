package com.fleety.olympics.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fleety.olympics.dto.request.CompetitionRequestDTO;
import com.fleety.olympics.dto.response.CompetitionResponseDTO;
import com.fleety.olympics.dto.response.PageResponseDTO;
import com.fleety.olympics.exception.DuplicateResourceException;
import com.fleety.olympics.exception.GlobalExceptionHandler;
import com.fleety.olympics.exception.ResourceNotFoundException;
import com.fleety.olympics.model.Competition.StatusCompetition;
import com.fleety.olympics.service.interfaces.ReadableService;
import com.fleety.olympics.service.interfaces.WritableService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(CompetitionController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("CompetitionController — Tests d'Intégration")
@ActiveProfiles("test")
class CompetitionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReadableService<CompetitionResponseDTO> readableService;

    @MockitoBean
    private WritableService<CompetitionResponseDTO, CompetitionRequestDTO> writableService;

    private CompetitionResponseDTO responseDTO;
    private CompetitionRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new CompetitionResponseDTO(
                1L, "100m Hommes", "Athlétisme",
                LocalDate.of(2026, 7, 26),
                LocalDate.of(2026, 7, 26),
                StatusCompetition.PLANIFIEE
        );

        requestDTO = new CompetitionRequestDTO();
        requestDTO.setNom("100m Hommes");
        requestDTO.setDiscipline("Athlétisme");
        requestDTO.setDateDebut(LocalDate.of(2026, 7, 26));
        requestDTO.setDateFin(LocalDate.of(2026, 7, 26));
        requestDTO.setStatut(StatusCompetition.PLANIFIEE);
    }

    @Nested
    @DisplayName("GET /api/v2/competitions")
    class GetAll {

        @Test
        @DisplayName("doit retourner 200 avec la liste des compétitions")
        void shouldReturn200_withCompetitionsList() throws Exception {

            PageResponseDTO<CompetitionResponseDTO> pageResponse = new PageResponseDTO<>(
                    List.of(responseDTO),
                    0,
                    1,
                    1L,
                    10,
                    true,
                    true
            );

            when(readableService.getAll(any(Pageable.class))).thenReturn(pageResponse);

            mockMvc.perform(get("/api/v2/competitions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.contenu[0].nom").value("100m Hommes"))
                    .andExpect(jsonPath("$.contenu[0].discipline").value("Athlétisme"))
                    .andExpect(jsonPath("$.contenu[0].statut").value("PLANIFIEE"))
                    .andExpect(jsonPath("$.pageActuelle").value(0))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.premiere").value(true))
                    .andExpect(jsonPath("$.derniere").value(true));;
        }

        @Test
        @DisplayName("doit retourner 200 avec une liste vide")
        void shouldReturn200_withEmptyList() throws Exception {

            PageResponseDTO<CompetitionResponseDTO> pageResponse = new PageResponseDTO<>(
                    List.of(),
                    0,
                    1,
                    0,
                    10,
                    true,
                    true
            );

            when(readableService.getAll(any(Pageable.class))).thenReturn(pageResponse);

            mockMvc.perform(get("/api/v2/competitions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.contenu").isEmpty())
                    .andExpect(jsonPath("$.pageActuelle").value(0))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.taillePage").value(10))
                    .andExpect(jsonPath("$.premiere").value(true))
                    .andExpect(jsonPath("$.derniere").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/v2/competitions/{id}")
    class GetById {

        @Test
        @DisplayName("doit retourner 200 quand la compétition existe")
        void shouldReturn200_whenExists() throws Exception {

            when(readableService.getById(1L)).thenReturn(responseDTO);

            mockMvc.perform(get("/api/v2/competitions/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nom").value("100m Hommes"))
                    .andExpect(jsonPath("$.statut").value("PLANIFIEE"));
        }

        @Test
        @DisplayName("doit retourner 404 quand la compétition n'existe pas")
        void shouldReturn404_whenNotFound() throws Exception {

            when(readableService.getById(99L))
                    .thenThrow(new ResourceNotFoundException("Compétition non trouvée avec l'id: 99"));

            mockMvc.perform(get("/api/v2/competitions/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Compétition non trouvée avec l'id: 99"));
        }
    }

    @Nested
    @DisplayName("POST /api/v2/competitions")
    class Create {

        @Test
        @DisplayName("doit retourner 201 quand les données sont valides")
        void shouldReturn201_whenValid() throws Exception {

            when(writableService.create(any())).thenReturn(responseDTO);

            mockMvc.perform(post("/api/v2/competitions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.nom").value("100m Hommes"))
                    .andExpect(jsonPath("$.statut").value("PLANIFIEE"));
        }

        @Test
        @DisplayName("doit retourner 400 quand le nom est vide")
        void shouldReturn400_whenNomIsBlank() throws Exception {

            requestDTO.setNom("");

            mockMvc.perform(post("/api/v2/competitions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Echec de validation"));
        }

        @Test
        @DisplayName("doit retourner 400 quand le statut est null")
        void shouldReturn400_whenStatutIsNull() throws Exception {

            requestDTO.setStatut(null);

            mockMvc.perform(post("/api/v2/competitions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Echec de validation"));
        }

        @Test
        @DisplayName("doit retourner 400 quand le corps est vide")
        void shouldReturn400_whenBodyIsEmpty() throws Exception {
            mockMvc.perform(post("/api/v2/competitions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Echec de validation"));
        }

        @Test
        @DisplayName("doit retourner 400 quand le corps est manquant")
        void shouldReturn400_whenBodyIsMissed() throws Exception {
            mockMvc.perform(post("/api/v2/competitions")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Contenu manquant ou malformé"));
        }

        @Test
        @DisplayName("doit retourner 409 quand le nom existe déjà")
        void shouldReturn409_whenNomAlreadyExists() throws Exception {

            when(writableService.create(any()))
                    .thenThrow(new DuplicateResourceException(
                            "Une compétition avec le nom '100m Hommes' existe déjà"));

            mockMvc.perform(post("/api/v2/competitions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.message").value("Une compétition avec le nom '100m Hommes' existe déjà"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v2/competitions/{id}")
    class Update {

        @Test
        @DisplayName("doit retourner 200 quand la mise à jour réussit")
        void shouldReturn200_whenUpdated() throws Exception {

            CompetitionResponseDTO updated = new CompetitionResponseDTO(
                    1L, "100m Hommes", "Athlétisme",
                    LocalDate.of(2026, 7, 26),
                    LocalDate.of(2026, 7, 26),
                    StatusCompetition.EN_COURS
            );
            when(writableService.update(eq(1L), any())).thenReturn(updated);

            requestDTO.setStatut(StatusCompetition.EN_COURS);

            mockMvc.perform(put("/api/v2/competitions/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statut").value("EN_COURS"));
        }

        @Test
        @DisplayName("doit retourner 404 quand la compétition n'existe pas")
        void shouldReturn404_whenNotFound() throws Exception {

            when(writableService.update(eq(99L), any()))
                    .thenThrow(new ResourceNotFoundException("Compétition non trouvée avec l'id: 99"));

            mockMvc.perform(put("/api/v2/competitions/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v2/competitions/{id}")
    class Delete {

        @Test
        @DisplayName("doit retourner 204 quand la suppression réussit")
        void shouldReturn204_whenDeleted() throws Exception {

            doNothing().when(writableService).delete(1L);

            mockMvc.perform(delete("/api/v2/competitions/1"))
                    .andExpect(status().isNoContent());

            verify(writableService, times(1)).delete(1L);
        }

        @Test
        @DisplayName("doit retourner 404 quand la compétition n'existe pas")
        void shouldReturn404_whenNotFound() throws Exception {

            doThrow(new ResourceNotFoundException("Compétition non trouvée avec l'id: 99"))
                    .when(writableService).delete(99L);

            mockMvc.perform(delete("/api/v2/competitions/99"))
                    .andExpect(status().isNotFound());
        }
    }
}