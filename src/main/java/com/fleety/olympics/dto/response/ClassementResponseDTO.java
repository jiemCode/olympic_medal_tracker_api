package com.fleety.olympics.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClassementResponseDTO {
    private long paysId;
    private String paysNom;
    private String paysCode;
    private String drapeau;
    private long or;
    private long argent;
    private long bronze;
    private long total;
    private long points;

    // Convenient ctor used in unit tests where the pays id is irrelevant
    public ClassementResponseDTO(String paysNom,
                                 String paysCode,
                                 String drapeau,
                                 long or,
                                 long argent,
                                 long bronze,
                                 long total,
                                 long points) {
        this(0L, paysNom, paysCode, drapeau, or, argent, bronze, total, points);
    }
}
