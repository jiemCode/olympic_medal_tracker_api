package com.fleety.olympics.strategy;

import com.fleety.olympics.dto.response.ClassementResponseDTO;
import org.springframework.stereotype.Component;
import java.util.Comparator;

@Component("total")
public class TriParTotal implements TriStrategy {

    @Override
    public Comparator<ClassementResponseDTO> comparator() {
        return Comparator.comparingLong(ClassementResponseDTO::getTotal).reversed();
    }
}