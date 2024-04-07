package org.brandon.petwellbackend.employee;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.brandon.petwellbackend.payload.APIResponse;
import org.brandon.petwellbackend.payload.AuthResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping("/auth/employees/register")
    @ResponseStatus(HttpStatus.CREATED)
    public APIResponse<AuthResponse> registerEmployee(@RequestBody @Valid EmployeeRegistrationRequest request) {
        return employeeService.registerEmployee(request);
    }

    @PostMapping("/auth/employees/login")
    public APIResponse<AuthResponse> loginEmployee(@RequestBody EmployeeLoginRequest request) {
        return employeeService.loginEmployee(request);
    }

    @GetMapping("/employees")
    @PreAuthorize("hasAnyAuthority('admin:read')")
    public APIResponse<List<EmployeeDTO>> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @GetMapping("/employees/{employee-id}")
    @PreAuthorize("hasAnyAuthority('admin:read')")
    public APIResponse<EmployeeDTO> getEmployeeById(@PathVariable("employee-id") Long id) {
        return employeeService.getEmployeeById(id);
    }

    @PutMapping("/employees/{employee-id}")
    @PreAuthorize("hasAnyAuthority('admin:update')")
    public APIResponse<EmployeeDTO> updateEmployee(@PathVariable("employee-id") Long id, @RequestBody @Valid EmployeeDTO employeeDto) {
        return employeeService.updateEmployee(id, employeeDto);
    }

    @DeleteMapping("/employees/{employee-id}")
    @PreAuthorize("hasAnyAuthority('admin:delete')")
    public APIResponse<?> deleteEmployee(@PathVariable("employee-id") Long id) {
        return employeeService.deleteEmployee(id);
    }
}
