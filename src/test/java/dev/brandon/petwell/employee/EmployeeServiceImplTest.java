package dev.brandon.petwell.employee;

import dev.brandon.petwell.exceptions.ApplicationException;
import dev.brandon.petwell.responses.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static dev.brandon.petwell.employee.JobTitle.VETERINARIAN_TECHNICIAN;
import static dev.brandon.petwell.enums.Role.ADMIN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeServiceImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void Should_Find_Employee_When_Valid_ID() {
        // Given
        Long id = 1L;

        Employee employee = new Employee(
                id,
                "John",
                "Doe",
                "john.doe@example.com",
                "password",
                VETERINARIAN_TECHNICIAN,
                ADMIN);

        EmployeeDTO employeeDto = new EmployeeDTO(
                "John",
                "Doe",
                "john.doe@example.com",
                "veterinarian_technician",
                "ADMIN");

        // When
        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));
        when(employeeMapper.mapToDTO(employee)).thenReturn(employeeDto);

        ApiResponse<EmployeeDTO> apiResponse = employeeServiceImpl.getEmployeeByID(id);

        // Then
        assertNotNull(apiResponse.getData());
        assertEquals(employeeDto.email(), apiResponse.getData().email());

        verify(employeeRepository, times(1)).findById(id);
    }

    @Test
    void Should_Find_All_Employees() {
        // Given
        Employee e1 = new Employee(
                1L,
                "John",
                "Doe",
                "john.doe@example.com",
                "password",
                VETERINARIAN_TECHNICIAN,
                ADMIN);

        Employee e2 = new Employee(
                2L,
                "Brandon",
                "Bryan",
                "brandon@example.com",
                "password",
                VETERINARIAN_TECHNICIAN,
                ADMIN);

        List<Employee> employees = List.of(e1, e2);

        // When
        when(employeeRepository.findAll()).thenReturn(employees);

        ApiResponse<List<EmployeeDTO>> response = employeeServiceImpl.getAllEmployees();

        // Then
        assertNotNull(response.getData());
        assertEquals(2, response.getData().size());

        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void Should_Update_Employee_When_Valid_ID_And_EmployeeDto() {
        // Given
        Long id = 1L;

        Employee employee = new Employee(
                id,
                "John",
                "Doe",
                "john.doe@example.com",
                "password",
                VETERINARIAN_TECHNICIAN,
                ADMIN);

        EmployeeDTO employeeDto = new EmployeeDTO(
                "Brandon",
                "Bryan",
                null,
                null,
                null);

        Employee updatedEmployee = new Employee(
                id,
                "Brandon",
                "Bryan",
                "john.doe@example.com",
                "password",
                VETERINARIAN_TECHNICIAN,
                ADMIN);

        EmployeeDTO updatedEmployeeDTO = new EmployeeDTO(
                "Brandon",
                "Bryan",
                "john.doe@example.com",
                VETERINARIAN_TECHNICIAN.getJobTitle(),
                ADMIN.name());

        // When
        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(updatedEmployee)).thenReturn(updatedEmployee);
        when(employeeMapper.mapToDTO(updatedEmployee)).thenReturn(updatedEmployeeDTO);

        ApiResponse<EmployeeDTO> response = employeeServiceImpl.updateEmployee(id, employeeDto);

        // Then
        assertNotNull(response.getData());
        assertEquals(updatedEmployee.getEmail(), response.getData().email());

        verify(employeeRepository, times(1)).findById(id);
        verify(employeeRepository, times(1)).save(updatedEmployee);
    }

    @Test
    void Should_Delete_Employee_When_Valid_ID() {
        // Given
        Long id = 1L;

        Employee employee = new Employee(
                id,
                "John",
                "Doe",
                "john.doe@example.com",
                "password",
                VETERINARIAN_TECHNICIAN,
                ADMIN);

        // When
        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));
        doNothing().when(employeeRepository).deleteById(id);

        employeeServiceImpl.deleteEmployee(id);
        
        // Then
        verify(employeeRepository, times(1)).deleteById(id);
    }

    @Test
    void Should_Throw_ApplicationException_When_Invalid_ID() {
        // Given
        Long id = 1L;

        // When
        when(employeeRepository.findById(id))
                .thenReturn(Optional.empty())
                .thenThrow(new ApplicationException(HttpStatus.NOT_FOUND, "Employee Not Found"));


        // Then
        ApplicationException applicationException = assertThrows(ApplicationException.class, () -> employeeServiceImpl.getEmployeeByID(id));
        assertEquals(applicationException.getHttpStatus(), HttpStatus.NOT_FOUND);

        verify(employeeRepository, times(1)).findById(id);
    }
}