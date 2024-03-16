package org.otis.dao.impl;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlResult;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.otis.dao.FruitDao;
import org.otis.model.dto.DtoRequest;
import org.otis.model.entity.Fruit;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class FruitDaoImpl implements FruitDao {
    @Inject
    PgPool client;

    @Override
    public Uni<List<Fruit>> findAll() {
        List<Fruit> fruits = new ArrayList<>();

        return client
                .query("SELECT id, name FROM fruits ORDER BY name ASC")
                .execute()
                .map(rows -> {
                    if (rows.size() > 0) {
                        rows.forEach(row -> fruits.add(new Fruit(row.getLong(0), row.getString(1))));
                    }

                    return fruits;
                });
    }

    @Override
    public Uni<Fruit> findById(Long id) {
        return client
                .preparedQuery("SELECT id, name FROM fruits WHERE id = $1")
                .execute(Tuple.of(id))
                .onItem().ifNotNull().transform(RowSet::iterator)
                .onItem().ifNotNull().transform(iterator -> iterator.hasNext() ? Fruit.from(iterator.next()) : null);
    }

    @Override
    public Uni<String> create(String name) {
        return client
                .preparedQuery("INSERT INTO fruits (name) VALUES ($1) RETURNING id")
                .execute(Tuple.of(name))
                .onItem().ifNotNull().transform(rows -> rows.iterator().next().getLong("id"))
                .onItem().ifNotNull().transform(longID -> URI.create("/fruits/" + longID).toString());
    }

    @Override
    public Uni<Fruit> patch(DtoRequest request) {
        return client
                .preparedQuery("UPDATE fruits SET name = $1 WHERE id = $2")
                .execute(Tuple.of(request.getName(), request.getId()))
                .onItem().ifNotNull().transform(SqlResult::rowCount)
                .onItem().ifNotNull().transform(integer -> integer > 0 ? new Fruit(request.getId(), request.getName()) : null);
    }

    @Override
    public Uni<Boolean> deleteById(Long id) {
        return client
                .preparedQuery("DELETE FROM fruits where id = $1")
                .execute(Tuple.of(id))
                .onItem().transform(rows -> rows.rowCount() == 1);
    }
}
