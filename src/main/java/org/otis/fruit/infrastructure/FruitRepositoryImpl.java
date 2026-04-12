package org.otis.fruit.infrastructure;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.otis.fruit.domain.Fruit;
import org.otis.fruit.domain.FruitRepository;
import org.otis.shared.constant.FilterParamNames;
import org.otis.shared.constant.PagingConstants;
import org.otis.shared.dto.DtoFruits;
import org.otis.shared.dto.DtoPagingRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.f4b6a3.uuid.UuidCreator;
import com.opengamma.elsql.ElSql;
import com.opengamma.elsql.ElSqlConfig;

import io.quarkus.runtime.util.StringUtil;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.SqlResult;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FruitRepositoryImpl implements FruitRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(FruitRepositoryImpl.class);
	private final Pool client;
	private final ElSql elSql;

	public FruitRepositoryImpl(Pool client) {
		this.client = client;
		URL resource = Thread.currentThread().getContextClassLoader().getResource("sql/fruits.elsql");
		if (resource == null) {
			throw new IllegalArgumentException("ELSql resource not found: sql/fruits.elsql");
		}
		this.elSql = ElSql.parse(ElSqlConfig.POSTGRES, resource);
	}

	private record QueryWithParams(String sql, Tuple tuple) {
	}

	/**
	 * Build query with positional params from a LinkedHashMap of named params.
	 */
	private QueryWithParams buildQuery(String sqlTemplate, LinkedHashMap<String, Object> params) {
		String sql = sqlTemplate;
		Tuple tuple = Tuple.tuple();
		int index = 1;
		for (Map.Entry<String, Object> entry : params.entrySet()) {
			sql = sql.replace(":" + entry.getKey(), "$" + index);
			tuple.addValue(entry.getValue());
			index++;
		}

		return new QueryWithParams(sql, tuple);
	}

	@Override
	public Uni<DtoFruits> getFruits(DtoPagingRequest pagingRequest, UUID id) {
		String orderColumn = getSafeOrderColumn(pagingRequest.getOrder());
		String sortDirection = getSafeSortDirection(pagingRequest.getSort());
		int limit = pagingRequest.getLimit() > 0 ? pagingRequest.getLimit() : PagingConstants.DEFAULT_LIMIT;
		int offset = pagingRequest.getOffset() >= 0 ? pagingRequest.getOffset() : PagingConstants.DEFAULT_OFFSET;

		Map<String, Object> params = new HashMap<>();
		params.put("orderColumn", orderColumn);
		params.put("sortDirection", sortDirection);
		params.put("limit", limit);
		params.put("offset", offset);

		if (id != null) {
			params.put(FilterParamNames.FRUIT_ID, "'" + id + "'");
		}

		if (!StringUtil.isNullOrEmpty(pagingRequest.getName())) {
			params.put(FilterParamNames.FRUIT_NAME, "'%" + pagingRequest.getName().toLowerCase() + "%'");
		}

		String sql = elSql.getSql("FindAllPaged", params);
		String sqlCount = elSql.getSql("CountAll", params);

		LOGGER.info(sql);
		LOGGER.info(sqlCount);

		Uni<List<Fruit>> listUni = client.query(sql).execute()
				.map(rows -> rows.stream()
						.map(row -> new Fruit(row.getUUID("id"), row.getString("name"))).toList());

		Uni<Long> countUni = client.query(sqlCount).execute()
				.map(rows -> rows.iterator().next().getLong(0));

		return Uni.combine().all().unis(listUni, countUni).asTuple().map(tuple2 -> {
			DtoFruits dtoFruits = new DtoFruits();
			dtoFruits.setFruits(tuple2.getItem1());
			dtoFruits.setCount(tuple2.getItem2().intValue());

			return dtoFruits;
		});
	}

	@Override
	public Uni<UUID> create(String name) {
		UUID uuid = UuidCreator.getTimeOrderedEpoch();
		String sqlTemplate = elSql.getSql("Create");
		var query = buildQuery(sqlTemplate, new LinkedHashMap<>(Map.of("id", uuid, "name", name)));

		return client.preparedQuery(query.sql).execute(query.tuple).onItem().ifNotNull()
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

		Map<String, Object> params = new HashMap<>();
		params.put("values", values.toString());

		String sql = elSql.getSql("CreateBulkBase", params);

		LOGGER.info(sql);

		return client.preparedQuery(sql).execute(tuple).onItem()
				.ifNotNull()
				.transform(SqlResult::rowCount);
	}

	@Override
	public Uni<Fruit> update(UUID id, String name) {
		String sqlTemplate = elSql.getSql("Update");
		var query = buildQuery(sqlTemplate, new LinkedHashMap<>(Map.of("name", name, "id", id)));

		return client.preparedQuery(query.sql).execute(query.tuple).onItem().ifNotNull()
				.transform(SqlResult::rowCount).onItem().ifNotNull()
				.transform(integer -> integer > 0 ? new Fruit(id, name) : null);
	}

	@Override
	public Uni<Boolean> deleteById(UUID id) {
		String sqlTemplate = elSql.getSql("DeleteById");
		var query = buildQuery(sqlTemplate, new LinkedHashMap<>(Map.of("id", id)));

		return client.preparedQuery(query.sql).execute(query.tuple).onItem()
				.transform(rows -> rows.rowCount() >= 1);
	}

	/**
	 * Validates sort direction against allowlist to prevent SQL injection.
	 */
	private String getSafeSortDirection(String sort) {
		if (StringUtil.isNullOrEmpty(sort)) {
			return "ASC";
		}

		String upperSort = sort.toUpperCase();
		if (PagingConstants.ALLOWED_SORT_DIRECTIONS.contains(upperSort)) {
			return upperSort;
		}

		throw new IllegalArgumentException("Invalid sort direction: " + sort + ". Must be ASC or DESC.");
	}

	/**
	 * Validates order column against known columns to prevent SQL injection. Throws
	 * exception if column name is not
	 * recognized.
	 */
	private String getSafeOrderColumn(String order) {
		if (StringUtil.isNullOrEmpty(order)) {
			return "name";
		}

		return switch (order) {
			case "id" -> "id";
			case "name" -> "name";
			default -> throw new IllegalArgumentException("Invalid order column: " + order);
		};
	}
}
