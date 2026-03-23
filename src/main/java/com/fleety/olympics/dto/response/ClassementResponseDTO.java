package com.fleety.olympics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClassementResponseDTO {
    private String paysNom;
    private String paysCode;
    private String drapeau;
    private long or;
    private long argent;
    private long bronze;
    private long total;
    private long points;
}