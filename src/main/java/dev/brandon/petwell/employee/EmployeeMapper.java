package dev.brandon.petwell.employee;

import dev.brandon.petwell.auth.RegisterRequest;
import dev.brandon.petwell.enums.Role;
import dev.brandon.petwell.exceptions.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class EmployeeMapper {

    private static final Logger log = LoggerFactory.getLogger(EmployeeMapper.class);

    private final PasswordEncoder passwordEncoder;

    public EmployeeMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public Employee mapToEmployee(RegisterRequest request) {
        String jobTitleEnumValue = request.jobTitle().toUpperCase();
        String roleEnumValue = request.role().toUpperCase();

        return new Employee(
                request.firstName(),
                request.lastName(),
                request.email(),
                passwordEncoder.encode(request.password()),
                JobTitle.valueOf(jobTitleEnumValue),
                Role.valueOf(roleEnumValue));
    }

    public EmployeeDTO mapToDTO(Employee employee) {
        if (employee == null) {
            log.error("Employee cannot be null");
            throw new ApplicationException(HttpStatus.BAD_REQUEST, "Bad Request");
        }

        return new EmployeeDTO(
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getJobTitle().name(),
                employee.getRole().name());
    }
}
