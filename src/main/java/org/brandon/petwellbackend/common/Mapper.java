package org.brandon.petwellbackend.common;

import lombok.RequiredArgsConstructor;
import org.brandon.petwellbackend.entity.Employee;
import org.brandon.petwellbackend.entity.Role;
import org.brandon.petwellbackend.enums.RoleType;
import org.brandon.petwellbackend.payload.EmployeeDTO;
import org.brandon.petwellbackend.payload.EmployeeRegistrationRequest;
import org.brandon.petwellbackend.enums.JobTitle;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class Mapper {
    private final PasswordEncoder passwordEncoder;

    public Employee toEmployee(EmployeeRegistrationRequest req) {
        JobTitle jobTitle = JobTitle.valueOf(req.jobTitle().toUpperCase());
        String roleNameUpperCase = req.role().toUpperCase();
        RoleType roleType = RoleType.valueOf(roleNameUpperCase);
        Role role = Role.builder().roleType(roleType).build();
        return Employee.builder()
                .userId(UUID.randomUUID().toString())
                .firstname(req.firstname())
                .lastname(req.lastname())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .jobTitle(jobTitle)
                .role(role)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .isEnabled(true)
                .build();
    }

    public EmployeeDTO toEmployeeDTO(Employee employee) {
        return EmployeeDTO.builder()
                .userId(employee.getUserId())
                .firstname(employee.getFirstname())
                .lastname(employee.getLastname())
                .email(employee.getEmail())
                .jobTitle(employee.getJobTitle().getTitle())
                .role(employee.getRole().getRoleType().getName())
                .isAccountNonExpired(employee.isAccountNonExpired())
                .isAccountNonLocked(employee.isAccountNonLocked())
                .isCredentialsNonExpired(employee.isCredentialsNonExpired())
                .isEnabled(employee.isEnabled())
                .build();
    }
}
