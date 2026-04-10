package org.otis.fruit.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.otis.fruit.domain.Fruit;
import org.otis.fruit.domain.FruitRepository;
import org.otis.shared.util.SqlManager;

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
	private final SqlManager sqlManager;

	public FruitRepositoryImpl(Pool client) {
		this.client = client;
		this.sqlManager = new SqlManager("sql/fruits.elsql");
	}

	@Override
	public Uni<List<Fruit>> findAll() {
		return client.query(sqlManager.getSql("FindAll")).execute()
				.map(rows -> rows.stream().map(row -> new Fruit(row.getUUID("id"), row.getString("name"))).toList());
	}

	@Override
	public Uni<Fruit> findById(UUID id) {
		String sql = sqlManager.getSql("FindById", Map.of("id", id));
		return client.preparedQuery(sql).execute(Tuple.of(id)).onItem()
				.ifNotNull().transform(RowSet::iterator).onItem().ifNotNull()
				.transform(iterator -> iterator.hasNext() ? Fruit.from(iterator.next()) : null);
	}

	@Override
	public Uni<UUID> create(String name) {
		UUID uuid = UuidCreator.getTimeOrderedEpoch();
		String sql = sqlManager.getSql("Create", Map.of("id", uuid, "name", name));
		return client.preparedQuery(sql).execute(Tuple.of(uuid, name)).onItem().ifNotNull()
				.transform(rows -> rows.iterator().next().getUUID("id"));
	}

	@Override
	public Uni<Integer> createBulk(List<String> names) {
		if (names == null || names.isEmpty()) {
			return Uni.createFrom().item(0);
		}

		// Build VALUES clause
		StringBuilder values = new StringBuilder();
		Tuple tuple = Tuple.tuple();

		for (int i = 0; i < names.size(); i++) {
			UUID uuid = UuidCreator.getTimeOrderedEpoch();
			String p1 = "$" + (i * 2 + 1);
			String p2 = "$" + (i * 2 + 2);
			if (i > 0) {
				values.append(", ");
			}
			values.append("(").append(p1).append(", ").append(p2).append(")");
			tuple.addUUID(uuid);
			tuple.addString(names.get(i));
		}

		String sqlTemplate = sqlManager.getSql("CreateBulkBase");
		String fullQuery = sqlTemplate.formatted(values);

		return client.preparedQuery(fullQuery).execute(tuple).onItem()
				.ifNotNull()
				.transform(SqlResult::rowCount);
	}

	@Override
	public Uni<Fruit> update(UUID id, String name) {
		String sql = sqlManager.getSql("Update", Map.of("name", name, "id", id));
		return client.preparedQuery(sql).execute(Tuple.of(name, id)).onItem().ifNotNull()
				.transform(SqlResult::rowCount).onItem().ifNotNull()
				.transform(integer -> integer > 0 ? new Fruit(id, name) : null);
	}

	@Override
	public Uni<Boolean> deleteById(UUID id) {
		String sql = sqlManager.getSql("DeleteById", Map.of("id", id));
		return client.preparedQuery(sql).execute(Tuple.of(id)).onItem()
				.transform(rows -> rows.rowCount() >= 1);
	}
}
