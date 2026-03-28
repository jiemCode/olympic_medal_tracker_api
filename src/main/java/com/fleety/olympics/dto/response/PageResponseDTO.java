package com.fleety.olympics.dto.response;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PageResponseDTO<T> {

    private List<T> contenu;
    private int pageActuelle;
    private int totalPages;
    private long totalElements;
    private int taillePage;
    private boolean premiere;
    private boolean derniere;

    // Factory method pour construit le DTO depuis un Page<T> Spring
    public static <T> PageResponseDTO<T> from(Page<T> page) {
        return new PageResponseDTO<>(
                page.getContent(),
                page.getNumber(),
                page.getTotalPages(),
                page.getTotalElements(),
                page.getSize(),
                page.isFirst(),
                page.isLast()
        );
    }
}