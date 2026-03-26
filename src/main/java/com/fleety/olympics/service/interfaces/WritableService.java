package com.fleety.olympics.service.interfaces;

public interface WritableService<T, D> {
    T create(D dto);
    T update(Long id, D dto);
    void delete(Long id);
}
