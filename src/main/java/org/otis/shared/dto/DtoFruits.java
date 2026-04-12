package org.otis.shared.dto;

import java.util.List;

import org.otis.fruit.domain.Fruit;

import lombok.Data;

@Data
public class DtoFruits {
    private List<Fruit> fruits;
    private int count;
}
