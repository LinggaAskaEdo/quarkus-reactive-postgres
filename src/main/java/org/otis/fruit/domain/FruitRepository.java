package org.otis.fruit.domain;

import java.util.List;
import java.util.UUID;

import org.otis.shared.dto.DtoFruits;
import org.otis.shared.dto.DtoPagingRequest;

import io.smallrye.mutiny.Uni;

public interface FruitRepository {
	Uni<DtoFruits> getFruits(DtoPagingRequest pagingRequest, UUID id);

	Uni<UUID> create(String name);

	Uni<Integer> createBulk(List<String> names);

	Uni<Fruit> update(UUID id, String name);

	Uni<Boolean> deleteById(UUID id);
}
