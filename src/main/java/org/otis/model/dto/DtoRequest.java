package org.otis.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtoRequest {
    @NotNull(groups = FruitPatch.class, message = "Id can't be null")
    @Min(groups = FruitPatch.class, message = "Id can't be less than 1", value = 1)
    private Long id;

    @NotNull(groups = {FruitCreate.class, FruitPatch.class}, message = "Name can't be null")
    @NotEmpty(groups = {FruitCreate.class, FruitPatch.class}, message = "Name can't be empty")
    private String name;
}
