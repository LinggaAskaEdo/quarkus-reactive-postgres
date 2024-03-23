package org.otis.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.otis.model.entity.Employee;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoEmployees {
    private List<Employee> employees;
    private int count;
}
