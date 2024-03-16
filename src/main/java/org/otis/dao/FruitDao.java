package org.otis.dao;

import io.smallrye.mutiny.Uni;
import org.otis.model.dto.DtoRequest;
import org.otis.model.entity.Fruit;

import java.util.List;

public interface FruitDao {
    Uni<List<Fruit>> findAll();

    Uni<Fruit> findById(Long id);

    Uni<String> create(String name);

    Uni<Fruit> patch(DtoRequest request);
}
