package org.otis.resource;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.groups.ConvertGroup;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.otis.model.dto.DtoRequest;
import org.otis.model.dto.DtoResponse;
import org.otis.model.dto.FruitCreate;
import org.otis.model.dto.FruitPatch;
import org.otis.service.FruitService;

@Path("fruits")
public class FruitController {
    @Inject
    FruitService fruitService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DtoResponse> get() {
        return fruitService.findAll();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public Uni<DtoResponse> getSingle(Long id) {
        return fruitService.findById(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DtoResponse> create(@Valid @ConvertGroup(to = FruitCreate.class) DtoRequest request) {
        return fruitService.create(request);
    }

    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DtoResponse> patch(@Valid @ConvertGroup(to = FruitPatch.class) DtoRequest request) {
        return fruitService.patch(request);
    }

    @DELETE
    @Path("{id}")
    public Uni<DtoResponse> delete(Long id) {
        return fruitService.deleteById(id);
    }
}
