package org.otis.fruit.domain;

import java.util.List;
import java.util.UUID;

import io.smallrye.mutiny.Uni;

public interface FruitRepository {
	Uni<List<Fruit>> findAll();

	Uni<Fruit> findById(UUID id);

	Uni<UUID> create(String name);

	Uni<Integer> createBulk(List<String> names);

	Uni<Fruit> update(UUID id, String name);

	Uni<Boolean> deleteById(UUID id);
}
