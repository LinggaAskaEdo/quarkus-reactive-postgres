package org.otis.model.dto;

import java.util.List;

import org.otis.model.entity.Employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DtoEmployees {
    private List<Employee> employees;
    private int count;
}
