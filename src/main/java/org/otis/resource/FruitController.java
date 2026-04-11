package org.otis.resource;

import java.util.UUID;

import org.otis.fruit.usecase.CreateFruit;
import org.otis.fruit.usecase.DeleteFruit;
import org.otis.fruit.usecase.FindAllFruits;
import org.otis.fruit.usecase.FindFruitById;
import org.otis.fruit.usecase.UpdateFruit;
import org.otis.shared.dto.DtoRequest;
import org.otis.shared.dto.DtoResponse;
import org.otis.shared.dto.FruitCreate;
import org.otis.shared.dto.FruitPatch;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("fruits")
@RolesAllowed("**")
public class FruitController {
	private final FindAllFruits findAllFruits;
	private final FindFruitById findFruitById;
	private final CreateFruit createFruit;
	private final UpdateFruit updateFruit;
	private final DeleteFruit deleteFruit;

	public FruitController(FindAllFruits findAllFruits, FindFruitById findFruitById, CreateFruit createFruit,
			UpdateFruit updateFruit, DeleteFruit deleteFruit) {
		this.findAllFruits = findAllFruits;
		this.findFruitById = findFruitById;
		this.createFruit = createFruit;
		this.updateFruit = updateFruit;
		this.deleteFruit = deleteFruit;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<DtoResponse> getAll() {
		return findAllFruits.execute();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}")
	public Uni<DtoResponse> getSingle(UUID id) {
		return findFruitById.execute(id);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<DtoResponse> create(@Valid @ConvertGroup(to = FruitCreate.class) DtoRequest request) {
		return createFruit.execute(request.getName());
	}

	@PATCH
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<DtoResponse> patch(@Valid @ConvertGroup(to = FruitPatch.class) DtoRequest request) {
		return updateFruit.execute(request.getId(), request.getName());
	}

	@DELETE
	@Path("{id}")
	public Uni<DtoResponse> delete(UUID id) {
		return deleteFruit.execute(id);
	}
}
