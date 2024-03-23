package org.otis.service.impl;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.otis.constant.CommonConstant;
import org.otis.constant.StatusMsgEnum;
import org.otis.dao.EmployeeDao;
import org.otis.model.dto.DtoPagingRequest;
import org.otis.model.dto.DtoPagingResponse;
import org.otis.model.entity.Employee;
import org.otis.model.vo.VoEmployees;
import org.otis.service.EmployeeService;
import org.otis.util.DtoHelper;

import java.util.List;

@ApplicationScoped
public class EmployeeServiceImpl implements EmployeeService {
    @Inject
    EmployeeDao employeeDao;

    @Override
    public Uni<DtoPagingResponse> getEmployees(DtoPagingRequest pagingRequest) {
        Uni<List<Employee>> employees = employeeDao.getEmployees(pagingRequest);

        return employees
                .onItem().ifNotNull().transform(resp -> DtoHelper.constructPagingResponse(StatusMsgEnum.SUCCESS, CommonConstant.SUCCESS, "Data found !!!", resp, resp.size(), resp.size()))
                .onItem().ifNull().continueWith(() -> DtoHelper.constructPagingResponse(StatusMsgEnum.FAILED, CommonConstant.FAILED, "Data not found !!!", null, 0, 0))
                .onFailure().recoverWithItem(failure -> DtoHelper.constructPagingResponse(StatusMsgEnum.FAILED, CommonConstant.FAILED, failure.getMessage(), null, 0, 0));
    }

    @Override
    public Uni<DtoPagingResponse> getAllEmployee(DtoPagingRequest pagingRequest) {
        Uni<VoEmployees> allEmployee = employeeDao.getAllEmployee(pagingRequest);

        return allEmployee
                .onItem().ifNotNull().transform(resp -> DtoHelper.constructPagingResponse(StatusMsgEnum.SUCCESS, CommonConstant.SUCCESS, "Data found !!!", resp.getEmployees(), resp.getEmployees().size(), resp.getCount()))
                .onItem().ifNull().continueWith(() -> DtoHelper.constructPagingResponse(StatusMsgEnum.FAILED, CommonConstant.FAILED, "Data not found !!!", null, 0, 0))
                .onFailure().recoverWithItem(failure -> DtoHelper.constructPagingResponse(StatusMsgEnum.FAILED, CommonConstant.FAILED, failure.getMessage(), null, 0, 0));
    }
}
