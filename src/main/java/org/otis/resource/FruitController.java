package org.otis.resource;

import io.quarkus.runtime.util.StringUtil;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.otis.constant.CommonConstant;
import org.otis.constant.StatusMsgEnum;
import org.otis.model.dto.DtoRequest;
import org.otis.model.dto.DtoResponse;
import org.otis.service.FruitService;
import org.otis.util.DtoHelper;

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
    public Uni<DtoResponse> create(DtoRequest request) {
        if (StringUtil.isNullOrEmpty(request.getName())) {
            return Uni.createFrom().item(DtoHelper.constructResponse(StatusMsgEnum.FAILED, CommonConstant.FAILED, "Invalid request !!!", null));
        }

        return fruitService.create(request);
    }

    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DtoResponse> patch(DtoRequest request) {
        if (null == request.getId() || StringUtil.isNullOrEmpty(String.valueOf(request.getId())) || request.getId() <= 0 || StringUtil.isNullOrEmpty(request.getName())) {
            return Uni.createFrom().item(DtoHelper.constructResponse(StatusMsgEnum.FAILED, CommonConstant.FAILED, "Invalid request !!!", null));
        }

        return fruitService.patch(request);
    }

    @DELETE
    @Path("{id}")
    public Uni<DtoResponse> delete(Long id) {
        return fruitService.deleteById(id);
    }
}
