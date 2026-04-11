package org.otis.resource;

import org.otis.employee.usecase.GetAllEmployees;
import org.otis.employee.usecase.GetEmployees;
import org.otis.shared.dto.DtoPagingRequest;
import org.otis.shared.dto.DtoPagingResponse;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("employees")
@RolesAllowed("**")
public class EmployeeController {
	private final GetEmployees getEmployees;
	private final GetAllEmployees getAllEmployees;

	public EmployeeController(GetEmployees getEmployees, GetAllEmployees getAllEmployees) {
		this.getEmployees = getEmployees;
		this.getAllEmployees = getAllEmployees;
	}

	@POST
	@Path("/1")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<DtoPagingResponse> getEmployees(DtoPagingRequest pagingRequest) {
		return getEmployees.execute(pagingRequest);
	}

	@POST
	@Path("/2")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<DtoPagingResponse> getAllEmployee(DtoPagingRequest pagingRequest) {
		return getAllEmployees.execute(pagingRequest);
	}
}
