package org.otis.resource;

import java.util.UUID;

import org.otis.model.dto.DtoRequest;
import org.otis.model.dto.DtoResponse;
import org.otis.model.dto.FruitCreate;
import org.otis.model.dto.FruitPatch;
import org.otis.service.FruitService;

import io.smallrye.mutiny.Uni;
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
public class FruitController {
    private final FruitService fruitService;

    public FruitController(FruitService fruitService) {
        this.fruitService = fruitService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DtoResponse> get() {
        return fruitService.findAll();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public Uni<DtoResponse> getSingle(UUID id) {
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
    public Uni<DtoResponse> delete(UUID id) {
        return fruitService.deleteById(id);
    }
}
