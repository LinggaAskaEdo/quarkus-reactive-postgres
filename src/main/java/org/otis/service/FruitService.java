package org.otis.service;

import io.smallrye.mutiny.Uni;
import org.otis.model.dto.DtoRequest;
import org.otis.model.dto.DtoResponse;

public interface FruitService {
    Uni<DtoResponse> findAll();

    Uni<DtoResponse> findById(Long id);

    Uni<DtoResponse> create(DtoRequest request);

    Uni<DtoResponse> patch(DtoRequest request);
}
