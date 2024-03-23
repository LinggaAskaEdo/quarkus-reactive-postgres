package org.otis.service;

import io.smallrye.mutiny.Uni;
import org.otis.model.dto.DtoPagingRequest;
import org.otis.model.dto.DtoPagingResponse;

public interface EmployeeService {
    Uni<DtoPagingResponse> getEmployees(DtoPagingRequest pagingRequest);

    Uni<DtoPagingResponse> getAllEmployee(DtoPagingRequest pagingRequest);
}
