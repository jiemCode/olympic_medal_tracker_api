package com.fleety.olympics.controller;

import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fleety.olympics.dto.request.MedailleRequestDTO;
import com.fleety.olympics.dto.response.ClassementResponseDTO;
import com.fleety.olympics.dto.response.MedailleResponseDTO;
import com.fleety.olympics.exception.GlobalExceptionHandler;
import com.fleety.olympics.exception.ResourceNotFoundException;
import com.fleety.olympics.model.Medaille.TypeMedaille;
import com.fleety.olympics.service.interfaces.Classifiable;
import com.fleety.olympics.service.interfaces.MedailleFilterable;
import com.fleety.olympics.service.interfaces.ReadableService;
import com.fleety.olympics.service.interfaces.WritableService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(MedailleController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("MedailleController — Tests d'Intégration")
class MedailleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReadableService<MedailleResponseDTO> readableService;

    @MockitoBean
    private WritableService<MedailleResponseDTO, MedailleRequestDTO> writableService;

    @MockitoBean
    private Classifiable classifiable;

    @MockitoBean
    private MedailleFilterable medailleFilterable;

    private MedailleResponseDTO medailleDTO;
    private MedailleRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        medailleDTO = new MedailleResponseDTO(
                1L, TypeMedaille.OR,
                LocalDate.of(2026, 8, 5),
                "Faye Mbaye", "Mbaye",
                "Sénégal", "Lutte 65kg"
        );

        requestDTO = new MedailleRequestDTO();
        requestDTO.setType(TypeMedaille.OR);
        requestDTO.setDateObtention(LocalDate.of(2026, 8, 5));
        requestDTO.setAthleteId(1L);
        requestDTO.setPaysId(1L);
        requestDTO.setCompetitionId(1L);
    }

    @Nested
    @DisplayName("GET /api/v1/medailles")
    class GetAll {

        @Test
        @DisplayName("doit retourner 200 avec la liste des médailles")
        void shouldReturn200_withMedaillesList() throws Exception {

            when(readableService.getAll()).thenReturn(List.of(medailleDTO));

            mockMvc.perform(get("/api/v1/medailles"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].type").value("OR"))
                    .andExpect(jsonPath("$[0].paysNom").value("Sénégal"))
                    .andExpect(jsonPath("$[0].competitionNom").value("Lutte 65kg"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/medailles/{id}")
    class Get {

        @Test
        @DisplayName("doit retourner 200 avec une médaille")
        void shouldReturn200_withOneMedaille() throws Exception {

            when(readableService.getById(1L)).thenReturn(medailleDTO);

            mockMvc.perform(get("/api/v1/medailles/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.type").exists())
                    .andExpect(jsonPath("$.dateObtention").exists())
                    .andExpect(jsonPath("$.athleteNom").exists())
                    .andExpect(jsonPath("$.athletePrenom").exists())
                    .andExpect(jsonPath("$.paysNom").exists())
                    .andExpect(jsonPath("$.competitionNom").exists());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/medailles")
    class Create {

        @Test
        @DisplayName("doit retourner 201 quand les données sont valides")
        void shouldReturn201_whenValid() throws Exception {

            when(writableService.create(any())).thenReturn(medailleDTO);

            mockMvc.perform(post("/api/v1/medailles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.type").value("OR"))
                    .andExpect(jsonPath("$.paysNom").value("Sénégal"));
        }

        @Test
        @DisplayName("doit retourner 400 quand le type est null")
        void shouldReturn400_whenTypeIsNull() throws Exception {

            requestDTO.setType(null);

            mockMvc.perform(post("/api/v1/medailles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Echec de validation"));
        }

        @Test
        @DisplayName("doit retourner 404 quand l'athlète n'existe pas")
        void shouldReturn404_whenAthleteNotFound() throws Exception {

            when(writableService.create(any()))
                    .thenThrow(new ResourceNotFoundException("Athlète non trouvé avec l'id: 99"));

            mockMvc.perform(post("/api/v1/medailles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Athlète non trouvé avec l'id: 99"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/medailles/{id}")
    class Update {

        @Test
        @DisplayName("doit retourner 200 quand les données sont valides")
        void shouldReturn201_whenValid() throws Exception {

            when(writableService.update(1L, requestDTO)).thenReturn(medailleDTO);

            mockMvc.perform(put("/api/v1/medailles/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.type").value("OR"))
                    .andExpect(jsonPath("$.paysNom").value("Sénégal"));
        }

        @Test
        @DisplayName("doit retourner 400 quand le type est null")
        void shouldReturn400_whenTypeIsNull() throws Exception {

            requestDTO.setType(null);

            mockMvc.perform(post("/api/v1/medailles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Echec de validation"));
        }

        @Test
        @DisplayName("doit retourner 404 quand l'athlète n'existe pas")
        void shouldReturn404_whenAthleteNotFound() throws Exception {

            when(writableService.create(any()))
                    .thenThrow(new ResourceNotFoundException("Athlète non trouvé avec l'id: 99"));

            mockMvc.perform(post("/api/v1/medailles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Athlète non trouvé avec l'id: 99"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/medailles/athlete/{athleteId}")
    class GetByAthlete {

        @Test
        @DisplayName("doit retourner 200 avec les médailles de l'athlète")
        void shouldReturn200_withAthleteMedailles() throws Exception {

            when(medailleFilterable.getByAthlete(1L)).thenReturn(List.of(medailleDTO));

            mockMvc.perform(get("/api/v1/medailles/athlete/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].athleteNom").value("Faye Mbaye"));
        }

        @Test
        @DisplayName("doit retourner 404 quand l'athlète n'existe pas")
        void shouldReturn404_whenAthleteNotFound() throws Exception {

            when(medailleFilterable.getByAthlete(99L))
                    .thenThrow(new ResourceNotFoundException("Athlète non trouvé avec l'id: 99"));

            mockMvc.perform(get("/api/v1/medailles/athlete/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/medailles/competition/{competitionId}")
    class GetByCompetition {

        @Test
        @DisplayName("doit retourner 200 avec les médailles de la compétition")
        void shouldReturn200_withCompetitionMedailles() throws Exception {

            when(medailleFilterable.getByCompetition(1L)).thenReturn(List.of(medailleDTO));

            mockMvc.perform(get("/api/v1/medailles/competition/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].type").exists())
                    .andExpect(jsonPath("$[0].dateObtention").exists())
                    .andExpect(jsonPath("$[0].athleteNom").exists())
                    .andExpect(jsonPath("$[0].athletePrenom").exists())
                    .andExpect(jsonPath("$[0].paysNom").exists())
                    .andExpect(jsonPath("$[0].competitionNom").exists());
        }

        @Test
        @DisplayName("doit retourner 404 quand la compétition n'existe pas")
        void shouldReturn404_whenCompetitionNotFound() throws Exception {

            when(medailleFilterable.getByCompetition(99L))
                    .thenThrow(new ResourceNotFoundException("Compétition non trouvé avec l'id: 99"));

            mockMvc.perform(get("/api/v1/medailles/competition/99"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/classement")
    class GetClassement {

        @Test
        @DisplayName("doit retourner 200 avec le classement par défaut")
        void shouldReturn200_withDefaultClassement() throws Exception {

            ClassementResponseDTO senegal = new ClassementResponseDTO(
                    "Sénégal", "SEN", "🇸🇳", 2L, 1L, 1L, 4L, 9L
            );
            ClassementResponseDTO usa = new ClassementResponseDTO(
                    "USA", "USA", "🇺🇸", 1L, 2L, 0L, 3L, 7L
            );
            when(classifiable.getClassement(null)).thenReturn(List.of(senegal, usa));

            mockMvc.perform(get("/api/v1/classement"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].paysNom").value("Sénégal"))
                    .andExpect(jsonPath("$[0].or").value(2))
                    .andExpect(jsonPath("$[0].points").value(9))
                    .andExpect(jsonPath("$[1].paysNom").value("USA"));
        }

        @Test
        @DisplayName("doit retourner 200 avec le classement trié par or")
        void shouldReturn200_sortedByOr() throws Exception {

            ClassementResponseDTO senegal = new ClassementResponseDTO(
                    "Sénégal", "SEN", "🇸🇳", 3L, 0L, 0L, 3L, 9L
            );
            when(classifiable.getClassement("or")).thenReturn(List.of(senegal));

            mockMvc.perform(get("/api/v1/classement?tri=or"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].or").value(3));
        }

        @Test
        @DisplayName("doit retourner 200 avec le classement trié par points")
        void shouldReturn200_sortedByPoints() throws Exception {

            ClassementResponseDTO senegal = new ClassementResponseDTO(
                    "Sénégal", "SEN", "🇸🇳", 2L, 1L, 1L, 4L, 9L
            );
            when(classifiable.getClassement("points")).thenReturn(List.of(senegal));

            mockMvc.perform(get("/api/v1/classement?tri=points"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].points").value(9));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/classement/pays/{paysId}")
    class GetStatsOfPays {

        @Test
        @DisplayName("doit retourner 200 avec les stats du pays")
        void shouldReturn200_withStatsOfPays() throws Exception {

            ClassementResponseDTO stats = new ClassementResponseDTO(
                    "Sénégal", "SEN", "🇸🇳", 2L, 1L, 1L, 4L, 9L
            );
            when(classifiable.getStatsByPays(1L)).thenReturn(stats);

            mockMvc.perform(get("/api/v1/classement/pays/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.paysNom").value("Sénégal"))
                    .andExpect(jsonPath("$.paysCode").value("SEN"))
                    .andExpect(jsonPath("$.or").value(2))
                    .andExpect(jsonPath("$.argent").value(1))
                    .andExpect(jsonPath("$.bronze").value(1))
                    .andExpect(jsonPath("$.total").value(4))
                    .andExpect(jsonPath("$.points").value(9));
        }

        @Test
        @DisplayName("doit retourner 404 quand le pays n'existe pas")
        void shouldReturn200_sortedByOrInPays() throws Exception {

            when(classifiable.getStatsByPays(99L)).thenThrow(new ResourceNotFoundException("Pays non trouvé avec l'id: 99"));

            mockMvc.perform(get("/api/v1/classement/pays/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Pays non trouvé avec l'id: 99"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/medailles/{id}")
    class Delete {

        @Test
        @DisplayName("doit retourner 204 quand la suppression réussit")
        void shouldReturn204_whenDeleted() throws Exception {

            doNothing().when(writableService).delete(1L);

            mockMvc.perform(delete("/api/v1/medailles/1"))
                    .andExpect(status().isNoContent());

            verify(writableService, times(1)).delete(1L);
        }

        @Test
        @DisplayName("doit retourner 404 quand la médaille n'existe pas")
        void shouldReturn404_whenNotFound() throws Exception {

            doThrow(new ResourceNotFoundException("Médaille non trouvée avec l'id: 99"))
                    .when(writableService).delete(99L);

            mockMvc.perform(delete("/api/v1/medailles/99"))
                    .andExpect(status().isNotFound());
        }
    }
}