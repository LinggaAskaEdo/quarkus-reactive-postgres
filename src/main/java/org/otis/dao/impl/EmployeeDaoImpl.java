package org.otis.dao.impl;

import io.quarkus.runtime.util.StringUtil;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.pgclient.PgPool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.otis.dao.EmployeeDao;
import org.otis.model.dto.DtoPagingRequest;
import org.otis.model.entity.Employee;
import org.otis.model.vo.VoEmployees;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class EmployeeDaoImpl implements EmployeeDao {
    @Inject
    PgPool client;

    @Override
    public Uni<List<Employee>> getEmployees(DtoPagingRequest pagingRequest) {
        List<Employee> employees = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT " +
                "   a.employee_id, " +
                "   a.first_name, " +
                "   a.last_name, " +
                "   a.email, " +
                "   a.phone, " +
                "   a.job_title " +
                "FROM " +
                "   employees a ");

        if (StringUtil.isNullOrEmpty(pagingRequest.getOrder())) {
            sql.append("ORDER BY a.employee_id ");
        } else {
            sql.append("ORDER BY ").append(switchOrder(pagingRequest.getOrder())).append(" ");
        }

        if (StringUtil.isNullOrEmpty(pagingRequest.getSort())) {
            sql.append("ASC ");
        } else {
            sql.append(pagingRequest.getSort().toUpperCase()).append(" ");
        }

        if (pagingRequest.getLimit() > 0) {
            sql.append("LIMIT ").append(pagingRequest.getLimit()).append(" ");
        } else {
            sql.append("LIMIT 10 ");
        }

        if (pagingRequest.getOffset() >= 0) {
            sql.append("OFFSET ").append(pagingRequest.getOffset()).append(" ");
        } else {
            sql.append("OFFSET 10 ");
        }

        return client
                .query(sql.toString())
                .execute()
                .map(rows -> {
                    if (rows.size() > 0) {
                        rows.forEach(row -> employees.add(new Employee(row.getLong(0), row.getString(1), row.getString(2), row.getString(3), row.getString(4), row.getString(5))));
                    }

                    return employees;
                });
    }

    @Override
    @SuppressWarnings("unchecked")
    public Uni<VoEmployees> getAllEmployee(DtoPagingRequest pagingRequest) {
        List<Employee> employees = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT " +
                "   employee_id, " +
                "   first_name, " +
                "   last_name, " +
                "   email, " +
                "   phone, " +
                "   job_title " +
                "FROM " +
                "   employees ");

        String sqlCount = "SELECT " +
                "   count(*) " +
                "FROM " +
                "   employees ";

        if (StringUtil.isNullOrEmpty(pagingRequest.getOrder())) {
            sql.append("ORDER BY employee_id ");
        } else {
            sql.append("ORDER BY ").append(switchOrder(pagingRequest.getOrder())).append(" ");
        }

        if (StringUtil.isNullOrEmpty(pagingRequest.getSort())) {
            sql.append("ASC ");
        } else {
            sql.append(pagingRequest.getSort().toUpperCase()).append(" ");
        }

        if (pagingRequest.getLimit() > 0) {
            sql.append("LIMIT ").append(pagingRequest.getLimit()).append(" ");
        } else {
            sql.append("LIMIT 10 ");
        }

        if (pagingRequest.getOffset() >= 0) {
            sql.append("OFFSET ").append(pagingRequest.getOffset()).append(" ");
        } else {
            sql.append("OFFSET 10 ");
        }

        Uni<List<Employee>> listUni = client
                .query(sql.toString())
                .execute()
                .map(rows -> {
                    if (rows.size() > 0) {
                        rows.forEach(row -> employees.add(new Employee(row.getLong(0), row.getString(1), row.getString(2), row.getString(3), row.getString(4), row.getString(5))));
                    }

                    return employees;
                });

        Uni<Integer> integerUni = client
                .query(sqlCount)
                .execute()
                .map(rows -> rows.iterator().next().getInteger(0));

        return Uni.combine().all().unis(listUni, integerUni)
                .with(
                        listOfResponses -> {
                            VoEmployees voEmployees = new VoEmployees();
                            voEmployees.setEmployees((List<Employee>) listOfResponses.get(0));
                            voEmployees.setCount((int) listOfResponses.get(1));

                            return voEmployees;
                        }
                );
    }

    private String switchOrder(String order) {
        return switch (order) {
            case "id" -> "employee_id";
            case "firstName" -> "first_name";
            case "lastName" -> "last_name";
            case "email" -> "email";
            case "phone" -> "phone";
            case "hireDate" -> "hire_date";
            case "jobTitle" -> "job_title";
            default -> null;
        };
    }
}
