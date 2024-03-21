package dev.brandon.petwell.employee;

import dev.brandon.petwell.employee.*;
import dev.brandon.petwell.enums.Role;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void Should_Save_Employee_When_Valid_Employee() {
        NewEmployeeRequest request = NewEmployeeRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("password")
                .jobTitle("veterinarian_technician")
                .role("admin")
                .build();

        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("password")
                .jobTitle(JobTitle.valueOf(request.jobTitle().toUpperCase()))
                .role(Role.valueOf(request.role().toUpperCase()))
                .build();

        EmployeeDto employeeDto = EmployeeDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .jobTitle("veterinarian_technician")
                .role("ADMIN")
                .build();

        when(employeeMapper.convertToEmployee(request)).thenReturn(employee);
        when(employeeRepository.save(employee)).thenReturn(employee);
        when(employeeMapper.convertToDto(employee)).thenReturn(employeeDto);

        ApiResponse<EmployeeDto> apiResponse = employeeService.saveEmployee(request);

        assertNotNull(apiResponse.getData());
        assertEquals(employeeDto.email(), apiResponse.getData().email());

        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void Should_Find_Employee_When_Valid_ID() {
        Long id = 1L;

        Employee employee = Employee.builder()
                .id(id)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .jobTitle(JobTitle.VETERINARIAN_TECHNICIAN)
                .build();

        EmployeeDto employeeDto = EmployeeDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .jobTitle("veterinarian_technician")
                .build();

        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));
        when(employeeMapper.convertToDto(employee)).thenReturn(employeeDto);

        ApiResponse<EmployeeDto> apiResponse = employeeService.findEmployeeByID(id);

        assertNotNull(apiResponse.getData());
        assertEquals(employeeDto.email(), apiResponse.getData().email());

        verify(employeeRepository, times(1)).findById(id);
    }

    @Test
    void Should_Find_All_Employees() {
        Employee e1 = Employee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .jobTitle(JobTitle.VETERINARIAN_TECHNICIAN)
                .build();

        Employee e2 = Employee.builder()
                .id(2L)
                .firstName("Brandon")
                .lastName("Bryan")
                .email("brandon@example.com")
                .jobTitle(JobTitle.VETERINARIAN)
                .build();

        EmployeeDto employeeDto1 = EmployeeDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .jobTitle("veterinarian_technician")
                .build();

        EmployeeDto employeeDto2 = EmployeeDto.builder()
                .firstName("Brandon")
                .lastName("Bryan")
                .email("brandon@example.com")
                .jobTitle("veterinarian")
                .build();

        List<Employee> employees = List.of(e1, e2);

        when(employeeRepository.findAll()).thenReturn(employees);
        when(employeeMapper.convertToDtoList(employees)).thenReturn(List.of(employeeDto1, employeeDto2));

        ApiResponse<List<EmployeeDto>> response = employeeService.findAllEmployees();

        assertNotNull(response.getData());
        assertEquals(2, response.getData().size());

        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void Should_Update_Employee_When_Valid_ID_And_EmployeeDto() {
        Long id = 1L;

        Employee employee = Employee.builder()
                .id(id)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("password")
                .jobTitle(JobTitle.VETERINARIAN_TECHNICIAN)
                .build();

        EmployeeDto employeeDto = EmployeeDto.builder()
                .firstName("Brandon")
                .lastName("Bryan")
                .build();

        Employee updatedEmployee = Employee.builder()
                .id(id)
                .firstName("Brandon")
                .lastName("Bryan")
                .email("john.doe@example.com")
                .password("password")
                .jobTitle(JobTitle.VETERINARIAN_TECHNICIAN)
                .build();

        EmployeeDto updatedEmployeeDto = EmployeeDto.builder()
                .firstName("Brandon")
                .lastName("Bryan")
                .email("john.doe@example.com")
                .jobTitle("veterinarian_technician")
                .build();


        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));
        when(employeeMapper.convertToEmployee(employeeDto, employee)).thenReturn(updatedEmployee);
        when(employeeRepository.save(updatedEmployee)).thenReturn(updatedEmployee);
        when(employeeMapper.convertToDto(updatedEmployee)).thenReturn(updatedEmployeeDto);

        ApiResponse<EmployeeDto> response = employeeService.updateEmployee(id, employeeDto);

        assertNotNull(response.getData());
        assertEquals(updatedEmployee.getEmail(), response.getData().email());

        verify(employeeRepository, times(1)).findById(id);
        verify(employeeRepository, times(1)).save(updatedEmployee);
    }

    @Test
    void Should_Delete_Employee_When_Valid_ID() {
        Long id = 1L;

        Employee employee = Employee.builder()
                .id(id)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("password")
                .jobTitle(JobTitle.VETERINARIAN_TECHNICIAN)
                .build();

        when(employeeRepository.findById(id)).thenReturn(Optional.of(employee));
        doNothing().when(employeeRepository).deleteById(id);

        employeeService.deleteEmployee(id);

        verify(employeeRepository, times(1)).deleteById(id);
    }

    @Test
    void Should_Throw_EmployeeAlreadyExistsException_When_Email_Already_Exists() {
        NewEmployeeRequest request = NewEmployeeRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("password")
                .jobTitle("veterinarian")
                .build();

        when(employeeRepository.existsByEmail(request.email())).thenThrow(new ApplicationException(HttpStatus.CONFLICT, "Employee Already Exists", null));

        assertThrows(ApplicationException.class, () -> employeeService.saveEmployee(request));

        verify(employeeRepository, times(1)).existsByEmail(request.email());
    }

    @Test
    void Should_Throw_EmployeeNotFoundException_When_Invalid_ID() {
        Long id = 1L;

        when(employeeRepository.findById(id))
                .thenReturn(Optional.empty())
                .thenThrow(new ApplicationException(HttpStatus.NOT_FOUND, "Employee Not Found"));

        assertThrows(ApplicationException.class, () -> employeeService.findEmployeeByID(id));

        verify(employeeRepository, times(1)).findById(id);
    }
}