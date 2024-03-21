package dev.brandon.petwell.employee;

import dev.brandon.petwell.enums.Role;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
@NoArgsConstructor
public class EmployeeMapper {

    public EmployeeDto convertToDto(Employee employee) {
        return EmployeeDto.builder()
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .email(employee.getEmail())
                .jobTitle(employee.getJobTitle().name())
                .role(employee.getRole().name())
                .build();
    }

    public Employee convertToEmployee(NewEmployeeRequest request) {
        String jobTitleEnumValue = request.jobTitle().toUpperCase();
        String roleEnumValue = request.role().toUpperCase();

        return Employee.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(request.password())
                .jobTitle(JobTitle.valueOf(jobTitleEnumValue))
                .role(Role.valueOf(roleEnumValue))
                .build();
    }

    public Employee convertToEmployee(EmployeeDto employeeDto, Employee employee) {
        return Employee.builder()
                .id(employee.getId())
                .firstName(returnNonNullValue(employeeDto::firstName, employee::getFirstName))
                .lastName(returnNonNullValue(employeeDto::lastName, employee::getLastName))
                .email(returnNonNullValue(employeeDto::email, employee::getEmail))
                .password(employee.getPassword())
                .jobTitle(employee.getJobTitle())
                .role(employee.getRole())
                .build();
    }

    public List<EmployeeDto> convertToDtoList(List<Employee> employees) {
        return employees.stream()
                .map(this::convertToDto)
                .toList();
    }

    private static String returnNonNullValue(Supplier<String> input, Supplier<String> existingValue) {
        return input.get() != null ? input.get() : existingValue.get();
    }
}
