package dev.brandon.petwell.employee;

import dev.brandon.petwell.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("api/v1/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('admin:read')")
    public ResponseEntity<ApiResponse<List<EmployeeDTO>>> getEmployees() {
        ApiResponse<List<EmployeeDTO>> response = employeeService.getAllEmployees();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{employee-id}")
    @PreAuthorize("hasAnyAuthority('admin:read')")
    public ResponseEntity<ApiResponse<EmployeeDTO>> getEmployeeById(@PathVariable("employee-id") Long id) {
        ApiResponse<EmployeeDTO> response = employeeService.getEmployeeByID(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping(path = "/{employee-id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('admin:update')")
    public ResponseEntity<ApiResponse<EmployeeDTO>> updateEmployee(@PathVariable("employee-id") Long id, @RequestBody @Valid EmployeeDTO employeeDto) {
        ApiResponse<EmployeeDTO> response = employeeService.updateEmployee(id, employeeDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{employee-id}")
    @PreAuthorize("hasAnyAuthority('admin:delete')")
    public ResponseEntity<ApiResponse<Object>> deleteEmployee(@PathVariable("employee-id") Long id) {
        ApiResponse<Object> response = employeeService.deleteEmployee(id);
        return ResponseEntity.ok(response);
    }
}
