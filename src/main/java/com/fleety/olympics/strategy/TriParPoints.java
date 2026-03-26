package com.fleety.olympics.strategy;

import com.fleety.olympics.dto.response.ClassementResponseDTO;
import org.springframework.stereotype.Component;
import java.util.Comparator;

@Component("points")
public class TriParPoints implements TriStrategy {

    @Override
    public Comparator<ClassementResponseDTO> comparator() {
        return Comparator.comparingLong(ClassementResponseDTO::getPoints).reversed();
    }
}