package org.otis.fruit.infrastructure;

import java.util.List;
import java.util.UUID;

import org.otis.fruit.domain.Fruit;
import org.otis.fruit.domain.FruitRepository;

import com.github.f4b6a3.uuid.UuidCreator;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlResult;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FruitRepositoryImpl implements FruitRepository {
	private final Pool client;

	public FruitRepositoryImpl(Pool client) {
		this.client = client;
	}

	@Override
	public Uni<List<Fruit>> findAll() {
		return client.query("SELECT id, name FROM fruits ORDER BY name ASC").execute()
				.map(rows -> rows.stream().map(row -> new Fruit(row.getUUID("id"), row.getString("name"))).toList());
	}

	@Override
	public Uni<Fruit> findById(UUID id) {
		return client.preparedQuery("SELECT id, name FROM fruits WHERE id = $1").execute(Tuple.of(id)).onItem()
				.ifNotNull().transform(RowSet::iterator).onItem().ifNotNull()
				.transform(iterator -> iterator.hasNext() ? Fruit.from(iterator.next()) : null);
	}

	@Override
	public Uni<UUID> create(String name) {
		UUID uuid = UuidCreator.getTimeOrderedEpoch();
		return client.preparedQuery("INSERT INTO fruits (id, name) VALUES ($1, $2) RETURNING id")
				.execute(Tuple.of(uuid, name)).onItem().ifNotNull()
				.transform(rows -> rows.iterator().next().getUUID("id"));
	}

	@Override
	public Uni<Integer> createBulk(List<String> names) {
		if (names == null || names.isEmpty()) {
			return Uni.createFrom().item(0);
		}

		// Build batch INSERT query with multiple values
		StringBuilder query = new StringBuilder("INSERT INTO fruits (id, name) VALUES ");
		Tuple tuple = Tuple.tuple();

		for (int i = 0; i < names.size(); i++) {
			UUID uuid = UuidCreator.getTimeOrderedEpoch();
			String placeholder1 = "$" + (i * 2 + 1);
			String placeholder2 = "$" + (i * 2 + 2);
			if (i > 0) {
				query.append(", ");
			}

			query.append("(").append(placeholder1).append(", ").append(placeholder2).append(")");
			tuple.addUUID(uuid);
			tuple.addString(names.get(i));
		}

		return client.preparedQuery(query.toString() + " ON CONFLICT (name) DO NOTHING").execute(tuple).onItem()
				.ifNotNull()
				.transform(SqlResult::rowCount);
	}

	@Override
	public Uni<Fruit> update(UUID id, String name) {
		return client.preparedQuery("UPDATE fruits SET name = $1 WHERE id = $2")
				.execute(Tuple.of(name, id)).onItem().ifNotNull()
				.transform(SqlResult::rowCount).onItem().ifNotNull()
				.transform(integer -> integer > 0 ? new Fruit(id, name) : null);
	}

	@Override
	public Uni<Boolean> deleteById(UUID id) {
		return client.preparedQuery("DELETE FROM fruits where id = $1").execute(Tuple.of(id)).onItem()
				.transform(rows -> rows.rowCount() >= 1);
	}
}
