package dev.brandon.petwell.employee;

import dev.brandon.petwell.exceptions.ApplicationException;
import dev.brandon.petwell.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final static Logger LOGGER = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
    }

    @Override
    public ApiResponse<List<EmployeeDTO>> getAllEmployees() {
        List<Employee> employees = employeeRepository.findAll();

        if (employees.isEmpty()) {
            LOGGER.error("No employees found");
            throw new ApplicationException(HttpStatus.NOT_FOUND, "No Employees Found", null);
        }

        List<EmployeeDTO> employeeDTOList = employees
                .stream()
                .map(employeeMapper::mapToDTO)
                .collect(Collectors.toList());

        return ApiResponse.successfulResponse("Successful", employeeDTOList);
    }

    @Override
    public ApiResponse<EmployeeDTO> getEmployeeByID(Long id) {
        EmployeeDTO employeeDto = employeeRepository.findById(id)
                .map(employeeMapper::mapToDTO)
                .orElseThrow(() -> throwEmployeeNotFoundException(id));

        return ApiResponse.successfulResponse("Successful", employeeDto);
    }

    @Override
    public ApiResponse<EmployeeDTO> updateEmployee(Long id, EmployeeDTO employeeDto) {
        EmployeeDTO updatedEmployeeDTO = employeeRepository.findById(id)
                .map(employee -> updateEmployeeWithDTOValues(employeeDto, employee))
                .map(employeeRepository::save)
                .map(employeeMapper::mapToDTO)
                .orElseThrow(() -> throwEmployeeNotFoundException(id));

        return ApiResponse.successfulResponse("Successful", updatedEmployeeDTO);
    }

    @Override
    public ApiResponse<Object> deleteEmployee(Long id) {
        Employee foundEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> throwEmployeeNotFoundException(id));

        employeeRepository.deleteById(foundEmployee.getId());

        return ApiResponse.successfulResponse("Successful");
    }

    private static ApplicationException throwEmployeeNotFoundException(Long id) {
        LOGGER.error("Employee with id {} not found.", id);
        return new ApplicationException(HttpStatus.NOT_FOUND, "Employee Not Found");
    }

    private static Employee updateEmployeeWithDTOValues(EmployeeDTO employeeDTO, Employee employee) {
        return new Employee(
                employee.getId(),
                returnNonNullValue(employeeDTO::firstName, employee::getFirstName),
                returnNonNullValue(employeeDTO::lastName, employee::getLastName),
                returnNonNullValue(employeeDTO::email, employee::getEmail),
                employee.getPassword(),
                employee.getJobTitle(),
                employee.getRole());
    }

    private static String returnNonNullValue(Supplier<String> input, Supplier<String> existingValue) {
        return input.get() != null ? input.get() : existingValue.get();
    }
}
