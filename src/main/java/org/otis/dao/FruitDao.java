package org.otis.dao;

import java.util.List;
import java.util.UUID;

import org.otis.model.dto.DtoRequest;
import org.otis.model.entity.Fruit;

import io.smallrye.mutiny.Uni;

public interface FruitDao {
    Uni<List<Fruit>> findAll();

    Uni<Fruit> findById(UUID id);

    Uni<UUID> create(String name);

    Uni<Fruit> patch(DtoRequest request);

    Uni<Boolean> deleteById(UUID id);
}
