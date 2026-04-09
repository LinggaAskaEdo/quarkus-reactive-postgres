package org.otis.service;

import org.otis.model.dto.DtoPagingRequest;
import org.otis.model.dto.DtoPagingResponse;

import io.smallrye.mutiny.Uni;

public interface EmployeeService {
    Uni<DtoPagingResponse> getEmployees(DtoPagingRequest pagingRequest);

    Uni<DtoPagingResponse> getAllEmployee(DtoPagingRequest pagingRequest);
}
