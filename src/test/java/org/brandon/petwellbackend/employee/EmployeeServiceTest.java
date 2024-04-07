package org.brandon.petwellbackend.employee;

import org.brandon.petwellbackend.common.Mapper;
import org.brandon.petwellbackend.exceptions.EntityNotFoundException;
import org.brandon.petwellbackend.payload.APIResponse;
import org.brandon.petwellbackend.payload.AuthResponse;
import org.brandon.petwellbackend.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.brandon.petwellbackend.common.Role.ADMIN;
import static org.brandon.petwellbackend.employee.JobTitle.VETERINARIAN;
import static org.brandon.petwellbackend.employee.JobTitle.VETERINARIAN_TECHNICIAN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private Mapper mapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void should_SuccessfullyLoginEmployee_When_AuthenticatingEmployeeWithValidCredentials() {
        // Arrange
        EmployeeLoginRequest employeeLoginRequest = EmployeeLoginRequest.builder()
                .email("john@example.com")
                .password("password123")
                .build();

        Employee employee = Employee.builder()
                .firstname("John")
                .lastname("Doe")
                .email(employeeLoginRequest.email())
                .password(employeeLoginRequest.password())
                .jobTitle(VETERINARIAN)
                .role(ADMIN)
                .build();

        when(employeeRepository.findByEmail(employeeLoginRequest.email())).thenReturn(Optional.of(employee));
        when(jwtService.generateJwtToken(any(UserDetails.class))).thenReturn("access_token");
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(employeeLoginRequest.email(),
                employeeLoginRequest.password()))).thenReturn(any(Authentication.class));

        // Act
        APIResponse<AuthResponse> actualResponse = employeeService.loginEmployee(employeeLoginRequest);

        // Assert
        assertEquals("Success", actualResponse.getMessage());

        verify(employeeRepository, times(1)).findByEmail(employeeLoginRequest.email());
        verify(jwtService, times(1)).generateJwtToken(any(UserDetails.class));
    }

    @Test
    void should_ThrowBadCredentialsException_When_AuthenticatingEmployeeWithInvalidCredentials() {
        // Arrange
        EmployeeLoginRequest employeeLoginRequest = EmployeeLoginRequest.builder()
                .email("john@example.com")
                .password("password123")
                .build();

        Employee employee = Employee.builder()
                .firstname("John")
                .lastname("Doe")
                .email(employeeLoginRequest.email())
                .password(employeeLoginRequest.password())
                .jobTitle(VETERINARIAN)
                .role(ADMIN)
                .build();

        when(employeeRepository.findByEmail(employeeLoginRequest.email())).thenReturn(Optional.of(employee));
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(employee.getEmail(),
                employee.getPassword()))).thenThrow(BadCredentialsException.class);

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> employeeService.loginEmployee(employeeLoginRequest));

        verify(employeeRepository, times(1)).findByEmail(employeeLoginRequest.email());
        verify(jwtService, times(0)).generateJwtToken(any(UserDetails.class));
    }

    @Test
    void should_ThrowBadCredentialsException_When_AuthenticatingEmployeeWithNonExistingEmail() {
        // Arrange
        EmployeeLoginRequest employeeLoginRequest = EmployeeLoginRequest.builder()
                .email("john@example.com")
                .password("password123")
                .build();

        when(employeeRepository.findByEmail(employeeLoginRequest.email())).thenReturn(Optional.empty());
        doThrow(BadCredentialsException.class).when(employeeRepository).findByEmail(employeeLoginRequest.email());

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> employeeService.loginEmployee(employeeLoginRequest));

        verify(employeeRepository, times(1)).findByEmail(employeeLoginRequest.email());
    }

    @Test
    void should_GetEmployee_When_ValidIDGiven() {
        // Arrange
        Long employeeId = 1L;

        Employee expectedEmployee = Employee.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN_TECHNICIAN)
                .role(ADMIN)
                .build();

        EmployeeDTO expectedEmployeeDto = EmployeeDTO.builder()
                .firstname(expectedEmployee.getFirstname())
                .lastname(expectedEmployee.getLastname())
                .email(expectedEmployee.getEmail())
                .jobTitle(expectedEmployee.getJobTitle().name())
                .role(expectedEmployee.getRole().name())
                .build();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(expectedEmployee));
        when(mapper.mapToDTO(expectedEmployee)).thenReturn(expectedEmployeeDto);

        // Act
        APIResponse<EmployeeDTO> actualResponse = employeeService.getEmployeeById(employeeId);

        // Assert
        assertEquals(expectedEmployeeDto, actualResponse.getData());
        assertEquals(expectedEmployeeDto.firstname(), actualResponse.getData().firstname());
        assertEquals(expectedEmployeeDto.lastname(), actualResponse.getData().lastname());
        assertEquals(expectedEmployeeDto.email(), actualResponse.getData().email());

        verify(employeeRepository, times(1)).findById(employeeId);
    }

    @Test
    void should_GetAllEmployees_When_EmployeesExist() {
        // Arrange
        Employee expectedEmployee1 = Employee.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN_TECHNICIAN)
                .role(ADMIN)
                .build();

        Employee expectedEmployee2 = Employee.builder()
                .id(2L)
                .firstname("Mary")
                .lastname("Smith")
                .email("mary@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN_TECHNICIAN)
                .role(ADMIN)
                .build();

        List<Employee> expectedEmployees = List.of(expectedEmployee1, expectedEmployee2);

        when(employeeRepository.findAll()).thenReturn(expectedEmployees);

        // Act
        APIResponse<List<EmployeeDTO>> actualResponse = employeeService.getAllEmployees();

        // Assert
        assertFalse(actualResponse.getData().isEmpty());
        assertEquals(expectedEmployees.size(), actualResponse.getData().size());

        verify(employeeRepository, times(1)).findAll();
    }

    @Test
    void should_UpdateEmployee_When_ValidIDAndEmployeeDtoGiven() {
        // Arrange
        Long idToUpdate = 1L;

        Employee existingEmployee = Employee.builder()
                .id(idToUpdate)
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN_TECHNICIAN)
                .role(ADMIN)
                .build();

        EmployeeDTO updatedEmployeeData = EmployeeDTO.builder()
                .firstname("Brandon")
                .lastname("Bryan")
                .build();

        Employee expectedUpdatedEmployee = Employee.builder()
                .id(idToUpdate)
                .firstname(updatedEmployeeData.firstname())
                .lastname(updatedEmployeeData.lastname())
                .email(existingEmployee.getEmail())
                .password(existingEmployee.getPassword())
                .jobTitle(existingEmployee.getJobTitle())
                .role(existingEmployee.getRole())
                .build();

        EmployeeDTO expectedUpdatedEmployeeDTO = EmployeeDTO.builder()
                .firstname(expectedUpdatedEmployee.getFirstname())
                .lastname(expectedUpdatedEmployee.getLastname())
                .email(expectedUpdatedEmployee.getEmail())
                .jobTitle(expectedUpdatedEmployee.getJobTitle().getTitle())
                .role(expectedUpdatedEmployee.getRole().getName())
                .build();

        when(employeeRepository.findById(idToUpdate)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(expectedUpdatedEmployee);
        when(mapper.mapToDTO(expectedUpdatedEmployee)).thenReturn(expectedUpdatedEmployeeDTO);

        // Act
        APIResponse<EmployeeDTO> actualResponse = employeeService.updateEmployee(idToUpdate, updatedEmployeeData);

        // Assert
        assertEquals("Success", actualResponse.getMessage());

        verify(employeeRepository, times(1)).findById(idToUpdate);
        verify(employeeRepository, times(1)).save(any(Employee.class));
        verify(mapper, times(1)).mapToDTO(expectedUpdatedEmployee);
    }

    @Test
    void should_DeleteEmployee_When_ValidIDGiven() {
        // Arrange
        Long idToDelete = 1L;

        Employee existingEmployee = Employee.builder()
                .id(idToDelete)
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN_TECHNICIAN)
                .role(ADMIN)
                .build();

        when(employeeRepository.findById(idToDelete)).thenReturn(Optional.of(existingEmployee));
        doNothing().when(employeeRepository).deleteById(idToDelete);

        // Act
        APIResponse<?> actualResponse = employeeService.deleteEmployee(idToDelete);

        // Assert
        assertEquals("Success", actualResponse.getMessage());

        verify(employeeRepository, times(1)).findById(idToDelete);
        verify(employeeRepository, times(1)).deleteById(idToDelete);
    }

    @Test
    void should_ThrowEntityNotFoundException_When_InvalidIDGiven() {
        // Arrange
        Long invalidId = 0L;

        when(employeeRepository.findById(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> employeeService.getEmployeeById(invalidId));

        assertEquals(String.format("Could not find employee with ID: %d", invalidId), exception.getMessage());

        verify(employeeRepository, times(1)).findById(invalidId);
    }
}