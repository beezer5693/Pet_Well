package org.brandon.petwellbackend.service;

import org.brandon.petwellbackend.entity.Employee;
import org.brandon.petwellbackend.payload.EmployeeDTO;
import org.brandon.petwellbackend.payload.EmployeeRegistrationRequest;

import java.util.List;

public interface EmployeeService {

    Employee registerEmployee(EmployeeRegistrationRequest registrationRequest);

    List<EmployeeDTO> getAllEmployees();

    EmployeeDTO getEmployeeById(Long id);

    EmployeeDTO getEmployeeByEmail(String email);

    EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDto);

    void deleteEmployee(Long id);
}
