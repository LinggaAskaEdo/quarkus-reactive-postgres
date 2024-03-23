package org.otis.dao;

import io.smallrye.mutiny.Uni;
import org.otis.model.dto.DtoPagingRequest;
import org.otis.model.entity.Employee;
import org.otis.model.vo.VoEmployees;

import java.util.List;

public interface EmployeeDao {
    Uni<List<Employee>> getEmployees(DtoPagingRequest pagingRequest);

    Uni<VoEmployees> getAllEmployee(DtoPagingRequest pagingRequest);
}
