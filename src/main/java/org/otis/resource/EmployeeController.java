package org.otis.resource;

import org.otis.model.dto.DtoPagingRequest;
import org.otis.model.dto.DtoPagingResponse;
import org.otis.service.EmployeeService;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

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
