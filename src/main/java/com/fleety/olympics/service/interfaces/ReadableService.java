package com.fleety.olympics.service.interfaces;

import org.springframework.data.domain.Pageable;

import com.fleety.olympics.dto.response.PageResponseDTO;

public interface ReadableService<T> {
    PageResponseDTO<T> getAll(Pageable pageable);
    T getById(Long id);
}