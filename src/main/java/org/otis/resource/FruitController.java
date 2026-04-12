package org.otis.resource;

import java.util.UUID;

import org.otis.fruit.usecase.CreateFruit;
import org.otis.fruit.usecase.DeleteFruit;
import org.otis.fruit.usecase.GetFruits;
import org.otis.fruit.usecase.UpdateFruit;
import org.otis.shared.dto.DtoPagingRequest;
import org.otis.shared.dto.DtoPagingResponse;
import org.otis.shared.dto.DtoRequest;
import org.otis.shared.dto.DtoResponse;
import org.otis.shared.dto.FruitCreate;
import org.otis.shared.dto.FruitPatch;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.validation.Valid;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("fruits")
@Authenticated
public class FruitController {
	private final GetFruits getFruits;
	private final CreateFruit createFruit;
	private final UpdateFruit updateFruit;
	private final DeleteFruit deleteFruit;

	public FruitController(GetFruits getFruits, CreateFruit createFruit,
			UpdateFruit updateFruit, DeleteFruit deleteFruit) {
		this.getFruits = getFruits;
		this.createFruit = createFruit;
		this.updateFruit = updateFruit;
		this.deleteFruit = deleteFruit;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<DtoPagingResponse> getFruits(
			@QueryParam("order") @DefaultValue("name") String order,
			@QueryParam("sort") @DefaultValue("ASC") String sort,
			@QueryParam("limit") @DefaultValue("10") int limit,
			@QueryParam("offset") @DefaultValue("0") int offset,
			@QueryParam("id") UUID id,
			@QueryParam("name") String name) {
		DtoPagingRequest pagingRequest = new DtoPagingRequest();
		pagingRequest.setOrder(order);
		pagingRequest.setSort(sort);
		pagingRequest.setLimit(limit);
		pagingRequest.setOffset(offset);
		pagingRequest.setName(name);

		return getFruits.execute(pagingRequest, id);
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
