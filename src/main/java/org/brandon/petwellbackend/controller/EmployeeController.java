package org.brandon.petwellbackend.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.brandon.petwellbackend.common.Mapper;
import org.brandon.petwellbackend.entity.Employee;
import org.brandon.petwellbackend.payload.*;
import org.brandon.petwellbackend.service.EmployeeService;
import org.brandon.petwellbackend.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;
    private final JwtService jwtService;
    private final Mapper mapper;

    @PostMapping("/auth/employees/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Response<EmployeeDTO> registerEmployee(@RequestBody @Valid EmployeeRegistrationRequest request, HttpServletResponse response) {
        Employee registeredEmployee = employeeService.registerEmployee(request);
        addJwtCookie(registeredEmployee, response);
        return Response.success(mapper.toEmployeeDTO(registeredEmployee), HttpStatus.CREATED);
    }

    @GetMapping("/employees")
    @PreAuthorize("hasAnyAuthority('admin:read')")
    public Response<List<EmployeeDTO>> getAllEmployees() {
        return Response.success(employeeService.getAllEmployees(), HttpStatus.OK);
    }

    @GetMapping("/employees/{employee-id}")
    @PreAuthorize("hasAnyAuthority('admin:read')")
    public Response<EmployeeDTO> getEmployeeById(@PathVariable("employee-id") Long id) {
        return Response.success(employeeService.getEmployeeById(id), HttpStatus.OK);
    }

    @PutMapping("/employees/{employee-id}")
    @PreAuthorize("hasAnyAuthority('admin:update')")
    public Response<EmployeeDTO> updateEmployee(@PathVariable("employee-id") Long id, @RequestBody @Valid EmployeeDTO employeeDto) {
        return Response.success(employeeService.updateEmployee(id, employeeDto), HttpStatus.OK);
    }

    @DeleteMapping("/employees/{employee-id}")
    @PreAuthorize("hasAnyAuthority('admin:delete')")
    public Response<?> deleteEmployee(@PathVariable("employee-id") Long id) {
        employeeService.deleteEmployee(id);
        return Response.success(null, HttpStatus.OK);
    }

    private void addJwtCookie(Employee employee, HttpServletResponse response) {
        jwtService.addCookie(response, employee);
    }
}
