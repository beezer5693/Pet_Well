package dev.brandon.petwell.employee;

import dev.brandon.petwell.responses.ApiResponse;

import java.util.List;

public interface EmployeeService {
    ApiResponse<List<EmployeeDTO>> getAllEmployees();

    ApiResponse<EmployeeDTO> getEmployeeByID(Long id);

    ApiResponse<EmployeeDTO> updateEmployee(Long id, EmployeeDTO employeeDto);

    ApiResponse<Object> deleteEmployee(Long id);
}
