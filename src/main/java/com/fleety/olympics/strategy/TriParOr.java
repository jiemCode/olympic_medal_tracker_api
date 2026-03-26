package com.fleety.olympics.strategy;

import java.util.Comparator;

import org.springframework.stereotype.Component;

import com.fleety.olympics.dto.response.ClassementResponseDTO;

@Component("or")
public class TriParOr implements TriStrategy {

    @Override
    public Comparator<ClassementResponseDTO> comparator() {
        return Comparator.comparingLong(ClassementResponseDTO::getOr).reversed();
    }
}
