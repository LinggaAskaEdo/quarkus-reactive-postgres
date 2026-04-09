package org.otis.shared.dto;

import java.util.List;

import org.otis.employee.domain.Employee;

import lombok.Data;

@Data
public class DtoEmployees {
    private List<Employee> employees;
    private int count;
}
