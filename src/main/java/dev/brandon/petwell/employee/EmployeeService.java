package dev.brandon.petwell.employee;

import dev.brandon.petwell.employee.EmployeeDto;
import dev.brandon.petwell.employee.NewEmployeeRequest;
import dev.brandon.petwell.responses.ApiResponse;

import java.util.List;

public interface EmployeeService {

    ApiResponse<EmployeeDto> saveEmployee(NewEmployeeRequest request);

    ApiResponse<EmployeeDto> findEmployeeByID(Long id);

    ApiResponse<List<EmployeeDto>> findAllEmployees();

    ApiResponse<EmployeeDto> updateEmployee(Long id, EmployeeDto employeeDTO);

    ApiResponse<Object> deleteEmployee(Long id);
}
