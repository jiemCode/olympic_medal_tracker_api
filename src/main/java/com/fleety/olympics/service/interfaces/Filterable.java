package com.fleety.olympics.service.interfaces;

import java.util.List;

public interface Filterable<T> {
    List<T> getByPays(Long paysId);
}
