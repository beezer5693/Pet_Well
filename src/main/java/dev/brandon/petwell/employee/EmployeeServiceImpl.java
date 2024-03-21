package dev.brandon.petwell.employee;

import dev.brandon.petwell.exceptions.ApplicationException;
import dev.brandon.petwell.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    public ApiResponse<EmployeeDto> saveEmployee(NewEmployeeRequest request) {
        boolean isEmployeeEmailExisting = employeeRepository.existsByEmail(request.email());

        if (isEmployeeEmailExisting) {
            log.error("Employee with email {} already exists", request.email());
            throw new ApplicationException(HttpStatus.CONFLICT, "Employee Already Exists", null);
        }

        Employee employee = employeeMapper.convertToEmployee(request);
        Employee savedEmployee = employeeRepository.save(employee);
        EmployeeDto employeeDto = employeeMapper.convertToDto(savedEmployee);

        return ApiResponse.successfulResponse(HttpStatus.CREATED.value(), "Successful", employeeDto);
    }

    @Override
    public ApiResponse<List<EmployeeDto>> findAllEmployees() {
        List<EmployeeDto> employees = employeeMapper.convertToDtoList(employeeRepository.findAll());

        if (employees.isEmpty()) {
            log.error("No employees found");
            throw new ApplicationException(HttpStatus.NOT_FOUND, "No Employees Found", null);
        }

        return ApiResponse.successfulResponse("Successful", employees);
    }

    @Override
    public ApiResponse<EmployeeDto> findEmployeeByID(Long id) {
        EmployeeDto employeeDto = employeeMapper.convertToDto(getExistingEmployee(id));
        return ApiResponse.successfulResponse("Successful", employeeDto);
    }

    @Override
    public ApiResponse<EmployeeDto> updateEmployee(Long id, EmployeeDto employeeDto) {
        Employee updatedEmployee = employeeMapper.convertToEmployee(employeeDto, getExistingEmployee(id));
        Employee savedEmployee = employeeRepository.save(updatedEmployee);
        EmployeeDto updatedEmployeeDto = employeeMapper.convertToDto(savedEmployee);

        return ApiResponse.successfulResponse("Successful", updatedEmployeeDto);
    }

    @Override
    public ApiResponse<Object> deleteEmployee(Long id) {
        employeeRepository.deleteById(getExistingEmployee(id).getId());
        return ApiResponse.successfulResponse("Successful", Map.of("id", id));
    }

    private Employee getExistingEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Employee with id {} not found.", id);
                    return new ApplicationException(HttpStatus.NOT_FOUND, "Employee Not Found");
                });
    }
}
