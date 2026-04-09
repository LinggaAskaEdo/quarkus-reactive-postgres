package org.otis.service;

import java.util.UUID;

import org.otis.model.dto.DtoRequest;
import org.otis.model.dto.DtoResponse;

import io.smallrye.mutiny.Uni;

public interface FruitService {
    Uni<DtoResponse> findAll();

    Uni<DtoResponse> findById(UUID id);

    Uni<DtoResponse> create(DtoRequest request);

    Uni<DtoResponse> patch(DtoRequest request);

    Uni<DtoResponse> deleteById(UUID id);
}
