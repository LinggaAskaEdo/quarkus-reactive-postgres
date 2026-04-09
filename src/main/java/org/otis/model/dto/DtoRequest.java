package org.otis.model.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtoRequest {
    @NotNull(groups = FruitPatch.class, message = "Id can't be null")
    private UUID id;

    @NotNull(groups = { FruitCreate.class, FruitPatch.class }, message = "Name can't be null")
    @NotEmpty(groups = { FruitCreate.class, FruitPatch.class }, message = "Name can't be empty")
    private String name;
}
