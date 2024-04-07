package org.brandon.petwellbackend.employee;

import org.brandon.petwellbackend.payload.APIResponse;
import org.brandon.petwellbackend.payload.AuthResponse;

import java.util.List;

public interface EmployeeService {
    APIResponse<AuthResponse> registerEmployee(EmployeeRegistrationRequest registrationRequest);

    APIResponse<AuthResponse> loginEmployee(EmployeeLoginRequest employeeLoginRequest);

    APIResponse<List<EmployeeDTO>> getAllEmployees();

    APIResponse<EmployeeDTO> getEmployeeById(Long id);

    APIResponse<EmployeeDTO> updateEmployee(Long id, EmployeeDTO employeeDto);

    APIResponse<?> deleteEmployee(Long id);
}
