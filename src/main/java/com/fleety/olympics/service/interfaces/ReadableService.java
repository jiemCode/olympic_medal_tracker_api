package com.fleety.olympics.service.interfaces;

import java.util.List;

public interface ReadableService<T> {
    List<T> getAll();
    T getById(Long id);
}