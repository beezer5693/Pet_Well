package org.brandon.petwellbackend.service;

import org.brandon.petwellbackend.common.Mapper;
import org.brandon.petwellbackend.entity.Employee;
import org.brandon.petwellbackend.entity.Role;
import org.brandon.petwellbackend.exception.EntityNotFoundException;
import org.brandon.petwellbackend.payload.EmployeeDTO;
import org.brandon.petwellbackend.payload.EmployeeLoginRequest;
import org.brandon.petwellbackend.repository.EmployeeRepository;
import org.brandon.petwellbackend.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.brandon.petwellbackend.enums.JobTitle.VETERINARIAN;
import static org.brandon.petwellbackend.enums.JobTitle.VETERINARIAN_TECHNICIAN;
import static org.brandon.petwellbackend.enums.RoleType.ADMIN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {
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
    void should_GetEmployee_When_ValidIDGiven() {
        // Arrange
        Long employeeId = 1L;

        Employee expectedEmployee = Employee.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN_TECHNICIAN)
                .role(Role.builder().roleType(ADMIN).build())
                .build();

        EmployeeDTO expectedEmployeeDto = EmployeeDTO.builder()
                .firstname(expectedEmployee.getFirstname())
                .lastname(expectedEmployee.getLastname())
                .email(expectedEmployee.getEmail())
                .jobTitle(expectedEmployee.getJobTitle().name())
                .role(expectedEmployee.getRole().getRoleType().name())
                .build();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(expectedEmployee));
        when(mapper.toEmployeeDTO(expectedEmployee)).thenReturn(expectedEmployeeDto);

        // Act
        EmployeeDTO employeeById = employeeService.getEmployeeById(employeeId);

        // Assert
        assertEquals(expectedEmployeeDto, employeeById);
        assertEquals(expectedEmployeeDto.firstname(), employeeById.firstname());
        assertEquals(expectedEmployeeDto.lastname(), employeeById.lastname());
        assertEquals(expectedEmployeeDto.email(), employeeById.email());

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
                .role(Role.builder().roleType(ADMIN).build())
                .build();

        Employee expectedEmployee2 = Employee.builder()
                .id(2L)
                .firstname("Mary")
                .lastname("Smith")
                .email("mary@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN_TECHNICIAN)
                .role(Role.builder().roleType(ADMIN).build())
                .build();

        List<Employee> expectedEmployeeEntities = List.of(expectedEmployee1, expectedEmployee2);

        when(employeeRepository.findAll()).thenReturn(expectedEmployeeEntities);

        // Act
        List<EmployeeDTO> employeeList = employeeService.getAllEmployees();

        // Assert
        assertFalse(employeeList.isEmpty());
        assertEquals(expectedEmployeeEntities.size(), employeeList.size());

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
                .role(Role.builder().roleType(ADMIN).build())
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
                .role(expectedUpdatedEmployee.getRole().getRoleType().getName())
                .build();

        when(employeeRepository.findById(idToUpdate)).thenReturn(Optional.of(existingEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(expectedUpdatedEmployee);
        when(mapper.toEmployeeDTO(expectedUpdatedEmployee)).thenReturn(expectedUpdatedEmployeeDTO);

        // Act
        EmployeeDTO updatedEmployee = employeeService.updateEmployee(idToUpdate, updatedEmployeeData);

        // Assert
        assertNotNull(updatedEmployee);
        assertEquals(expectedUpdatedEmployeeDTO, updatedEmployee);

        verify(employeeRepository, times(1)).findById(idToUpdate);
        verify(employeeRepository, times(1)).save(any(Employee.class));
        verify(mapper, times(1)).toEmployeeDTO(expectedUpdatedEmployee);
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
                .role(Role.builder().roleType(ADMIN).build())
                .build();

        when(employeeRepository.findById(idToDelete)).thenReturn(Optional.of(existingEmployee));
        doNothing().when(employeeRepository).deleteById(idToDelete);

        // Act
        employeeService.deleteEmployee(idToDelete);

        // Assert
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