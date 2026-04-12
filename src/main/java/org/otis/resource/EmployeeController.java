package org.otis.resource;

import org.otis.employee.usecase.GetEmployees;
import org.otis.shared.dto.DtoPagingRequest;
import org.otis.shared.dto.DtoPagingResponse;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("employees")
@Authenticated
public class EmployeeController {
	private final GetEmployees getEmployees;

	public EmployeeController(GetEmployees getEmployees) {
		this.getEmployees = getEmployees;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Uni<DtoPagingResponse> getEmployees(
			@QueryParam("order") @DefaultValue("id") String order,
			@QueryParam("sort") @DefaultValue("ASC") String sort,
			@QueryParam("limit") @DefaultValue("10") int limit,
			@QueryParam("offset") @DefaultValue("0") int offset,
			@QueryParam("firstName") String firstName,
			@QueryParam("lastName") String lastName,
			@QueryParam("email") String email) {
		DtoPagingRequest pagingRequest = new DtoPagingRequest();
		pagingRequest.setOrder(order);
		pagingRequest.setSort(sort);
		pagingRequest.setLimit(limit);
		pagingRequest.setOffset(offset);
		pagingRequest.setFirstName(firstName);
		pagingRequest.setLastName(lastName);
		pagingRequest.setEmail(email);

		return getEmployees.execute(pagingRequest);
	}
}
