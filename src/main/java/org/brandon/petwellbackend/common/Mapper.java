package org.brandon.petwellbackend.common;

import lombok.RequiredArgsConstructor;
import org.brandon.petwellbackend.employee.Employee;
import org.brandon.petwellbackend.employee.EmployeeDTO;
import org.brandon.petwellbackend.employee.EmployeeRegistrationRequest;
import org.brandon.petwellbackend.employee.JobTitle;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Mapper {

    private final PasswordEncoder passwordEncoder;

    public Employee mapToEmployee(EmployeeRegistrationRequest req) {
        String jobTitleEnumValue = req.jobTitle().toUpperCase();
        String roleEnumValue = req.role().toUpperCase();

        return Employee.builder()
                .firstname(req.firstname())
                .lastname(req.lastname())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .jobTitle(JobTitle.valueOf(jobTitleEnumValue))
                .role(Role.valueOf(roleEnumValue))
                .build();
    }

    public EmployeeDTO mapToDTO(Employee employee) {
        return EmployeeDTO.builder()
                .firstname(employee.getFirstname())
                .lastname(employee.getLastname())
                .email(employee.getEmail())
                .jobTitle(employee.getJobTitle().getTitle())
                .role(employee.getRole().getName())
                .build();
    }
}
