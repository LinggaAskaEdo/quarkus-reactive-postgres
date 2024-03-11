package org.otis.model.entity;

import io.vertx.mutiny.sqlclient.Row;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Fruit implements Serializable {
    private Long id;
    private String name;

    public static Fruit from(Row row) {
        return new Fruit(row.getLong("id"), row.getString("name"));
    }
}
