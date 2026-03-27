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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fleety.olympics.dto.request.AthleteRequestDTO;
import com.fleety.olympics.dto.response.AthleteResponseDTO;
import com.fleety.olympics.exception.GlobalExceptionHandler;
import com.fleety.olympics.exception.ResourceNotFoundException;
import com.fleety.olympics.service.interfaces.Filterable;
import com.fleety.olympics.service.interfaces.ReadableService;
import com.fleety.olympics.service.interfaces.WritableService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AthleteController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("AthleteController — Tests d'Intégration")
class AthleteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReadableService<AthleteResponseDTO> readableService;

    @MockitoBean
    private WritableService<AthleteResponseDTO, AthleteRequestDTO> writableService;

    @MockitoBean
    private Filterable<AthleteResponseDTO> filterable;

    private AthleteResponseDTO responseDTO;
    private AthleteRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new AthleteResponseDTO(
                1L, "Faye", "Mbaye",
                LocalDate.of(1998, 6, 14),
                "Lutte", 1L, "Sénégal", "SEN"
        );

        requestDTO = new AthleteRequestDTO();
        requestDTO.setNom("Faye");
        requestDTO.setPrenom("Mbaye");
        requestDTO.setDateNaissance(LocalDate.of(1998, 6, 14));
        requestDTO.setDiscipline("Lutte");
        requestDTO.setPaysId(1L);
    }

    @Nested
    @DisplayName("GET /api/v1/athletes")
    class GetAll {

        @Test
        @DisplayName("doit retourner 200 avec la liste des athlètes")
        void shouldReturn200_withAthletesList() throws Exception {

            when(readableService.getAll()).thenReturn(List.of(responseDTO));

            mockMvc.perform(get("/api/v1/athletes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].nom").value("Faye"))
                    .andExpect(jsonPath("$[0].prenom").value("Mbaye"))
                    .andExpect(jsonPath("$[0].paysNom").value("Sénégal"));
        }

        @Test
        @DisplayName("doit retourner 200 avec une liste vide")
        void shouldReturn200_withEmptyList() throws Exception {

            when(readableService.getAll()).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/athletes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/athletes/{id}")
    class GetById {

        @Test
        @DisplayName("doit retourner 200 quand l'athlète existe")
        void shouldReturn200_whenExists() throws Exception {

            when(readableService.getById(1L)).thenReturn(responseDTO);

            mockMvc.perform(get("/api/v1/athletes/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nom").value("Faye"))
                    .andExpect(jsonPath("$.discipline").value("Lutte"))
                    .andExpect(jsonPath("$.paysCode").value("SEN"));
        }

        @Test
        @DisplayName("doit retourner 404 quand l'athlète n'existe pas")
        void shouldReturn404_whenNotFound() throws Exception {

            when(readableService.getById(99L))
                    .thenThrow(new ResourceNotFoundException("Athlète non trouvé avec l'id: 99"));

            mockMvc.perform(get("/api/v1/athletes/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Athlète non trouvé avec l'id: 99"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/athletes/pays/{paysId}")
    class GetByPays {

        @Test
        @DisplayName("doit retourner 200 avec les athlètes du pays")
        void shouldReturn200_withAthletesByPays() throws Exception {

            when(filterable.getByPays(1L)).thenReturn(List.of(responseDTO));

            mockMvc.perform(get("/api/v1/athletes/pays/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].paysNom").value("Sénégal"))
                    .andExpect(jsonPath("$[0].paysCode").value("SEN"));
        }

        @Test
        @DisplayName("doit retourner 404 quand le pays n'existe pas")
        void shouldReturn404_whenPaysNotFound() throws Exception {

            when(filterable.getByPays(99L))
                    .thenThrow(new ResourceNotFoundException("Pays non trouvé avec l'id: 99"));

            mockMvc.perform(get("/api/v1/athletes/pays/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/athletes")
    class Create {

        @Test
        @DisplayName("doit retourner 201 quand les données sont valides")
        void shouldReturn201_whenValid() throws Exception {

            when(writableService.create(any())).thenReturn(responseDTO);

            mockMvc.perform(post("/api/v1/athletes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.nom").value("Faye"))
                    .andExpect(jsonPath("$.paysNom").value("Sénégal"));
        }

        @Test
        @DisplayName("doit retourner 400 quand le nom est vide")
        void shouldReturn400_whenNomIsBlank() throws Exception {

            requestDTO.setNom("");

            mockMvc.perform(post("/api/v1/athletes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Echec de validation"));
        }

        @Test
        @DisplayName("doit retourner 400 quand paysId est null")
        void shouldReturn400_whenPaysIdIsNull() throws Exception {

            requestDTO.setPaysId(null);

            mockMvc.perform(post("/api/v1/athletes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Echec de validation"));
        }

        @Test
        @DisplayName("doit retourner 400 quand le corps est vide")
        void shouldReturn400_whenBodyIsEmpty() throws Exception {
            mockMvc.perform(post("/api/v1/athletes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Echec de validation"));
        }

        @Test
        @DisplayName("doit retourner 400 quand le corps est manquant")
        void shouldReturn400_whenBodyIsMissed() throws Exception {
            mockMvc.perform(post("/api/v1/athletes")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Bad Request"))
                    .andExpect(jsonPath("$.message").value("Contenu manquant ou malformé"));
        }

        @Test
        @DisplayName("doit retourner 404 quand le pays n'existe pas")
        void shouldReturn404_whenPaysNotFound() throws Exception {

            when(writableService.create(any()))
                    .thenThrow(new ResourceNotFoundException("Pays non trouvé avec l'id: 99"));

            mockMvc.perform(post("/api/v1/athletes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Pays non trouvé avec l'id: 99"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/athletes/{id}")
    class Update {

        @Test
        @DisplayName("doit retourner 200 quand la mise à jour réussit")
        void shouldReturn200_whenUpdated() throws Exception {

            when(writableService.update(eq(1L), any())).thenReturn(responseDTO);

            mockMvc.perform(put("/api/v1/athletes/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nom").value("Faye"));
        }

        @Test
        @DisplayName("doit retourner 404 quand l'athlète n'existe pas")
        void shouldReturn404_whenNotFound() throws Exception {

            when(writableService.update(eq(99L), any()))
                    .thenThrow(new ResourceNotFoundException("Athlète non trouvé avec l'id: 99"));

            mockMvc.perform(put("/api/v1/athletes/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/athletes/{id}")
    class Delete {

        @Test
        @DisplayName("doit retourner 204 quand la suppression réussit")
        void shouldReturn204_whenDeleted() throws Exception {

            doNothing().when(writableService).delete(1L);

            mockMvc.perform(delete("/api/v1/athletes/1"))
                    .andExpect(status().isNoContent());

            verify(writableService, times(1)).delete(1L);
        }

        @Test
        @DisplayName("doit retourner 404 quand l'athlète n'existe pas")
        void shouldReturn404_whenNotFound() throws Exception {

            doThrow(new ResourceNotFoundException("Athlète non trouvé avec l'id: 99"))
                    .when(writableService).delete(99L);

            mockMvc.perform(delete("/api/v1/athletes/99"))
                    .andExpect(status().isNotFound());
        }
    }
}