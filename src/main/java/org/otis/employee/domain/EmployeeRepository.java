package org.otis.employee.domain;

import java.util.List;

import org.otis.shared.dto.DtoEmployees;
import org.otis.shared.dto.DtoPagingRequest;

import io.smallrye.mutiny.Uni;

public interface EmployeeRepository {
	Uni<List<Employee>> getEmployees(DtoPagingRequest pagingRequest);

	Uni<DtoEmployees> getAllEmployee(DtoPagingRequest pagingRequest);
}
