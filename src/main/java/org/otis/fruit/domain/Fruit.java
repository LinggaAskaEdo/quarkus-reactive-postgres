package org.otis.fruit.domain;

import java.util.UUID;

import io.vertx.mutiny.sqlclient.Row;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fruit {
	private UUID id;
	private String name;

	public static Fruit from(Row row) {
		return new Fruit(row.getUUID("id"), row.getString("name"));
	}
}
