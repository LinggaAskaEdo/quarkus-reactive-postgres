package org.otis.dao;

import java.util.List;

import org.otis.model.dto.DtoEmployees;
import org.otis.model.dto.DtoPagingRequest;
import org.otis.model.entity.Employee;

import io.smallrye.mutiny.Uni;

public interface EmployeeDao {
    Uni<List<Employee>> getEmployees(DtoPagingRequest pagingRequest);

    Uni<DtoEmployees> getAllEmployee(DtoPagingRequest pagingRequest);
}
