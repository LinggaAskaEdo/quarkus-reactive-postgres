package org.otis.shared.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DtoRequest {
	@NotNull(message = "ID is required for patch", groups = FruitPatch.class)
	private UUID id;

	@NotNull(message = "Name must not be null", groups = { FruitCreate.class, FruitPatch.class })
	@Size(min = 1, message = "Name must not be empty", groups = { FruitCreate.class, FruitPatch.class })
	private String name;
}
