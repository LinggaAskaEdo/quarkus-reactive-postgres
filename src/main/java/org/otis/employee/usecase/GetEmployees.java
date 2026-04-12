package org.otis.employee.usecase;

import org.otis.employee.domain.EmployeeRepository;
import org.otis.shared.constant.StatusMsgEnum;
import org.otis.shared.dto.DtoPagingRequest;
import org.otis.shared.dto.DtoPagingResponse;
import org.otis.shared.util.DtoHelper;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GetEmployees {
	private final EmployeeRepository employeeRepository;

	public GetEmployees(EmployeeRepository employeeRepository) {
		this.employeeRepository = employeeRepository;
	}

	public Uni<DtoPagingResponse> execute(DtoPagingRequest pagingRequest) {
		return employeeRepository.getEmployees(pagingRequest).onItem().ifNotNull()
				.transform(resp -> DtoHelper.constructPagingResponse(StatusMsgEnum.SUCCESS,
						"Data found !!!", resp.getEmployees(), resp.getEmployees().size(), resp.getCount()))
				.onItem().ifNull()
				.continueWith(() -> DtoHelper.constructPagingResponse(StatusMsgEnum.FAILED,
						"Data not found !!!", null, 0, 0))
				.onFailure().recoverWithItem(failure -> DtoHelper.constructPagingResponse(StatusMsgEnum.FAILED,
						failure.getMessage(), null, 0, 0));
	}
}
