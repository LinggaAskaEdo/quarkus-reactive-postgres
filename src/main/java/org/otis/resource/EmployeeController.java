package org.otis.resource;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.otis.model.dto.DtoPagingRequest;
import org.otis.model.dto.DtoPagingResponse;
import org.otis.service.EmployeeService;

@Path("employees")
public class EmployeeController {
    @Inject
    EmployeeService employeeService;

    @POST
    @Path("/1")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DtoPagingResponse> getEmployees(DtoPagingRequest pagingRequest) {
        return employeeService.getEmployees(pagingRequest);
    }

    @POST
    @Path("/2")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<DtoPagingResponse> getAllEmployee(DtoPagingRequest pagingRequest) {
        return employeeService.getAllEmployee(pagingRequest);
    }
}
