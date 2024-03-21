package dev.brandon.petwell.employee;

import dev.brandon.petwell.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<EmployeeDto>> save(@RequestBody @Valid NewEmployeeRequest request) {
        ApiResponse<EmployeeDto> response = employeeService.saveEmployee(request);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeDto>>> findAll() {
        ApiResponse<List<EmployeeDto>> response = employeeService.findAllEmployees();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/{employee-id}")
    public ResponseEntity<ApiResponse<EmployeeDto>> findByID(@PathVariable("employee-id") Long id) {
        ApiResponse<EmployeeDto> response = employeeService.findEmployeeByID(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping(path = "/{employee-id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<EmployeeDto>> update(@PathVariable("employee-id") Long id, @RequestBody @Valid EmployeeDto employeeDto) {
        ApiResponse<EmployeeDto> response = employeeService.updateEmployee(id, employeeDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{employee-id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable("employee-id") Long id) {
        ApiResponse<Object> response = employeeService.deleteEmployee(id);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
