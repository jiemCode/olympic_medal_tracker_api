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

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fleety.olympics.dto.request.PaysRequestDTO;
import com.fleety.olympics.dto.response.PageResponseDTO;
import com.fleety.olympics.dto.response.PaysResponseDTO;
import com.fleety.olympics.exception.DuplicateResourceException;
import com.fleety.olympics.exception.GlobalExceptionHandler;
import com.fleety.olympics.exception.ResourceNotFoundException;
import com.fleety.olympics.security.service.JwtService;
import com.fleety.olympics.service.PaysService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(PaysController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("PaysController — Tests d'Intégration")
@ActiveProfiles("test")
class PaysControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaysService paysService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    private PaysResponseDTO responseDTO;
    private PaysRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new PaysResponseDTO(1L, "Sénégal", "SEN", "🇸🇳");

        requestDTO = new PaysRequestDTO();
        requestDTO.setNom("Sénégal");
        requestDTO.setCode("SEN");
        requestDTO.setDrapeau("🇸🇳");
    }

    @Nested
    @DisplayName("GET /api/v2/pays")
    class GetAll {

        @Test
        @DisplayName("doit retourner 200 avec la liste des pays")
        void shouldReturn200_withPaysList() throws Exception {

            PageResponseDTO<PaysResponseDTO> pageResponse = new PageResponseDTO<>(
                    List.of(responseDTO),
                    0,
                    1,
                    1L,
                    10,
                    true,
                    true
            );

            when(paysService.getAll(any())).thenReturn(pageResponse);

            mockMvc.perform(get("/api/v2/pays"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenu").isArray())
                .andExpect(jsonPath("$.contenu[0].id").value(1))
                .andExpect(jsonPath("$.contenu[0].nom").value("Sénégal"))
                .andExpect(jsonPath("$.contenu[0].code").value("SEN"))
                .andExpect(jsonPath("$.pageActuelle").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.taillePage").value(10))
                .andExpect(jsonPath("$.premiere").value(true))
                .andExpect(jsonPath("$.derniere").value(true));
        }

        @Test
        @DisplayName("doit retourner 200 avec une liste vide")
        void shouldReturn200_withEmptyList() throws Exception {

            PageResponseDTO<PaysResponseDTO> pageVide = new PageResponseDTO<>(
                    List.of(),
                    0,
                    0,
                    0L,
                    10,
                    true,
                    true
            );

            when(paysService.getAll(any())).thenReturn(pageVide);

            mockMvc.perform(get("/api/v2/pays"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.contenu").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/v2/pays/{id}")
    class GetById {

        @Test
        @DisplayName("doit retourner 200 quand le pays existe")
        void shouldReturn200_whenExists() throws Exception {

            when(paysService.getById(1L)).thenReturn(responseDTO);

            mockMvc.perform(get("/api/v2/pays/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nom").value("Sénégal"))
                    .andExpect(jsonPath("$.code").value("SEN"))
                    .andExpect(jsonPath("$.drapeau").value("🇸🇳"));
        }

        @Test
        @DisplayName("doit retourner 404 quand le pays n'existe pas")
        void shouldReturn404_whenNotFound() throws Exception {

            when(paysService.getById(99L))
                    .thenThrow(new ResourceNotFoundException("Pays non trouvé avec l'id: 99"));

            mockMvc.perform(get("/api/v2/pays/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Pays non trouvé avec l'id: 99"));
        }
    }

    @Nested
    @DisplayName("POST /api/v2/pays")
    class Create {

        @Test
        @DisplayName("doit retourner 201 quand les données sont valides")
        void shouldReturn201_whenValid() throws Exception {

            when(paysService.create(any())).thenReturn(responseDTO);

            mockMvc.perform(post("/api/v2/pays")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.nom").value("Sénégal"));
        }

        @Test
        @DisplayName("doit retourner 400 quand le nom est vide")
        void shouldReturn400_whenNomIsBlank() throws Exception {

            requestDTO.setNom("");

            mockMvc.perform(post("/api/v2/pays")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("doit retourner 400 quand le code est vide")
        void shouldReturn400_whenCodeIsBlank() throws Exception {

            requestDTO.setCode("");

            mockMvc.perform(post("/api/v2/pays")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("doit retourner 400 quand le corps est vide")
        void shouldReturn400_whenBodyIsEmpty() throws Exception {
            mockMvc.perform(post("/api/v2/pays")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("doit retourner 409 quand le code existe déjà")
        void shouldReturn409_whenCodeAlreadyExists() throws Exception {

            when(paysService.create(any()))
                    .thenThrow(new DuplicateResourceException("Un pays avec le code 'SEN' existe déjà"));

            mockMvc.perform(post("/api/v2/pays")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.error").value("Conflict"))
                    .andExpect(jsonPath("$.message").value("Un pays avec le code 'SEN' existe déjà"));;
        }
    }

    @Nested
    @DisplayName("PUT /api/v2/pays/{id}")
    class Update {

        @Test
        @DisplayName("doit retourner 200 quand la mise à jour réussit")
        void shouldReturn200_whenUpdated() throws Exception {

            when(paysService.update(eq(1L), any())).thenReturn(responseDTO);

            mockMvc.perform(put("/api/v2/pays/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.nom").value("Sénégal"));
        }

        @Test
        @DisplayName("doit retourner 404 quand le pays n'existe pas")
        void shouldReturn404_whenNotFound() throws Exception {

            when(paysService.update(eq(99L), any()))
                    .thenThrow(new ResourceNotFoundException("Pays non trouvé avec l'id: 99"));

            mockMvc.perform(put("/api/v2/pays/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v2/pays/{id}")
    class Delete {

        @Test
        @DisplayName("doit retourner 204 quand la suppression réussit")
        void shouldReturn204_whenDeleted() throws Exception {

            doNothing().when(paysService).delete(1L);

            mockMvc.perform(delete("/api/v2/pays/1"))
                    .andExpect(status().isNoContent());

            verify(paysService, times(1)).delete(1L);
        }

        @Test
        @DisplayName("doit retourner 404 quand le pays n'existe pas")
        void shouldReturn404_whenNotFound() throws Exception {

            doThrow(new ResourceNotFoundException("Pays non trouvé avec l'id: 99"))
                    .when(paysService).delete(99L);

            mockMvc.perform(delete("/api/v2/pays/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }
}
