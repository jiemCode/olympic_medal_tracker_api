package com.fleety.olympics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaysResponseDTO {

    private Long id;
    private String nom;
    private String code;
    private String drapeau;
}