package com.fleety.olympics.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fleety.olympics.dto.response.ClassementResponseDTO;

@DisplayName("TriStrategy — Tests Unitaires")
@SpringBootTest
@ActiveProfiles("test")
class TriStrategyTest {

    private ClassementResponseDTO buildDTO(String code, long or, long argent, long bronze) {
        long total  = or + argent + bronze;
        long points = (or * 3) + (argent * 2) + bronze;
        return new ClassementResponseDTO("Pays-" + code, code, "🇸🇳", or, argent, bronze, total, points);
    }

    private List<ClassementResponseDTO> sorted(TriStrategy strategy, ClassementResponseDTO... dtos) {
        List<ClassementResponseDTO> list = Arrays.asList(dtos);
        list.sort(strategy.comparator());
        return list;
    }

    @Nested
    @DisplayName("TriParOr")
    class TriParOrTest {

        private final TriStrategy strategy = new TriParOr();

        @Test
        @DisplayName("doit trier par or décroissant")
        void shouldSortByOrDescending() {
            ClassementResponseDTO sen = buildDTO("SEN", 3, 0, 0);
            ClassementResponseDTO usa = buildDTO("USA", 1, 5, 0);
            ClassementResponseDTO fra = buildDTO("FRA", 2, 0, 0);

            List<ClassementResponseDTO> result = sorted(strategy, sen, usa, fra);

            assertThat(result).extracting(ClassementResponseDTO::getPaysCode)
                    .containsExactly("SEN", "FRA", "USA");
        }

        @Test
        @DisplayName("doit placer les ex-aequo dans un ordre stable")
        void shouldHandleTies() {
            ClassementResponseDTO a = buildDTO("A", 2, 0, 0);
            ClassementResponseDTO b = buildDTO("B", 2, 3, 0);

            List<ClassementResponseDTO> result = sorted(strategy, a, b);

            assertThat(result).extracting(ClassementResponseDTO::getOr)
                    .containsExactly(2L, 2L);
        }
    }

    @Nested
    @DisplayName("TriParArgent")
    class TriParArgentTest {

        private final TriStrategy strategy = new TriParArgent();

        @Test
        @DisplayName("doit trier par argent décroissant")
        void shouldSortByArgentDescending() {
            ClassementResponseDTO sen = buildDTO("SEN", 0, 1, 0);
            ClassementResponseDTO usa = buildDTO("USA", 0, 3, 0);
            ClassementResponseDTO fra = buildDTO("FRA", 0, 2, 0);

            List<ClassementResponseDTO> result = sorted(strategy, sen, usa, fra);

            assertThat(result).extracting(ClassementResponseDTO::getPaysCode)
                    .containsExactly("USA", "FRA", "SEN");
        }

        @Test
        @DisplayName("doit ignorer les médailles or et bronze dans le tri")
        void shouldIgnoreOrAndBronze() {
            ClassementResponseDTO a = buildDTO("A", 10, 1, 10);
            ClassementResponseDTO b = buildDTO("B", 0,  5, 0);

            List<ClassementResponseDTO> result = sorted(strategy, a, b);

            assertThat(result.get(0).getPaysCode()).isEqualTo("B");
        }
    }

    @Nested
    @DisplayName("TriParBronze")
    class TriParBronzeTest {

        private final TriStrategy strategy = new TriParBronze();

        @Test
        @DisplayName("doit trier par bronze décroissant")
        void shouldSortByBronzeDescending() {
            ClassementResponseDTO sen = buildDTO("SEN", 0, 0, 4);
            ClassementResponseDTO usa = buildDTO("USA", 0, 0, 1);
            ClassementResponseDTO fra = buildDTO("FRA", 0, 0, 2);

            List<ClassementResponseDTO> result = sorted(strategy, sen, usa, fra);

            assertThat(result).extracting(ClassementResponseDTO::getPaysCode)
                    .containsExactly("SEN", "FRA", "USA");
        }

        @Test
        @DisplayName("doit ignorer les médailles or et argent dans le tri")
        void shouldIgnoreOrAndArgent() {
            ClassementResponseDTO a = buildDTO("A", 10, 10, 1);
            ClassementResponseDTO b = buildDTO("B", 0,  0,  3);

            List<ClassementResponseDTO> result = sorted(strategy, a, b);

            assertThat(result.get(0).getPaysCode()).isEqualTo("B");
        }
    }

    @Nested
    @DisplayName("TriParTotal")
    class TriParTotalTest {

        private final TriStrategy strategy = new TriParTotal();

        @Test
        @DisplayName("doit trier par total décroissant")
        void shouldSortByTotalDescending() {
            ClassementResponseDTO sen = buildDTO("SEN", 1, 1, 1); // total=3
            ClassementResponseDTO usa = buildDTO("USA", 3, 2, 1); // total=6
            ClassementResponseDTO fra = buildDTO("FRA", 0, 0, 1); // total=1

            List<ClassementResponseDTO> result = sorted(strategy, sen, usa, fra);

            assertThat(result).extracting(ClassementResponseDTO::getPaysCode)
                    .containsExactly("USA", "SEN", "FRA");
        }

        @Test
        @DisplayName("doit sommer tous les types de médailles")
        void shouldSumAllMedalTypes() {
            ClassementResponseDTO a = buildDTO("A", 2, 2, 2); // total=6
            ClassementResponseDTO b = buildDTO("B", 3, 0, 0); // total=3

            List<ClassementResponseDTO> result = sorted(strategy, b, a);

            assertThat(result.get(0).getTotal()).isEqualTo(6L);
        }
    }

    @Nested
    @DisplayName("TriParPoints")
    class TriParPointsTest {

        private final TriStrategy strategy = new TriParPoints();

        @Test
        @DisplayName("doit trier par points décroissant")
        void shouldSortByPointsDescending() {
            // SEN: (1*3)+(0*2)+(0)=3  USA: (0*3)+(2*2)+(0)=4  FRA: (0*3)+(0*2)+(1)=1
            ClassementResponseDTO sen = buildDTO("SEN", 1, 0, 0);
            ClassementResponseDTO usa = buildDTO("USA", 0, 2, 0);
            ClassementResponseDTO fra = buildDTO("FRA", 0, 0, 1);

            List<ClassementResponseDTO> result = sorted(strategy, sen, usa, fra);

            assertThat(result).extracting(ClassementResponseDTO::getPaysCode)
                    .containsExactly("USA", "SEN", "FRA");
        }

        @Test
        @DisplayName("1 or doit valoir plus que 2 argent (3 > 4 — contre-exemple inversé)")
        void shouldWeightOrMoreThanArgent() {
            // or=1 → 3pts  vs  argent=2 → 4pts  => argent gagne ici
            ClassementResponseDTO orPays     = buildDTO("OR",     1, 0, 0); // 3 pts
            ClassementResponseDTO argentPays = buildDTO("ARGENT", 0, 2, 0); // 4 pts

            List<ClassementResponseDTO> result = sorted(strategy, orPays, argentPays);

            assertThat(result.get(0).getPaysCode()).isEqualTo("ARGENT");
        }

        @Test
        @DisplayName("1 or doit valoir plus que 1 argent + 1 bronze (3 > 3 — égalité)")
        void shouldHandleEqualPoints() {
            ClassementResponseDTO a = buildDTO("A", 1, 0, 0); // 3 pts
            ClassementResponseDTO b = buildDTO("B", 0, 1, 1); // 3 pts

            List<ClassementResponseDTO> result = sorted(strategy, a, b);

            assertThat(result).extracting(ClassementResponseDTO::getPoints)
                    .containsExactly(3L, 3L);
        }
    }
}
