package org.brandon.petwellbackend.employee;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.brandon.petwellbackend.common.Role.ADMIN;
import static org.brandon.petwellbackend.employee.JobTitle.VETERINARIAN_TECHNICIAN;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EmployeeRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void shouldEstablishConnectionToPostgresContainer() {
        assertTrue(postgreSQLContainer.isCreated());
        assertTrue(postgreSQLContainer.isRunning());
    }

    @Test
    void should_FindNoEmployees_When_RepositoryIsEmpty() {
        // Arrange & Act
        List<Employee> employees = employeeRepository.findAll();

        // Assert
        assertThat(employees).withFailMessage("The repository should be empty but it isn't.").isEmpty();
    }

    @Test
    void should_SaveEmployee_When_ValidEmployeeGiven() {
        // Arrange
        Employee e1 = Employee.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN_TECHNICIAN)
                .role(ADMIN)
                .build();

        // Act
        Employee savedEmployee = employeeRepository.save(e1);
        Long savedEmployeeID = savedEmployee.getId();
        Employee retrievedEmployee = employeeRepository.findById(savedEmployeeID).orElse(null);

        // Assert
        assertNotNull(retrievedEmployee);
        assertEquals(savedEmployeeID, retrievedEmployee.getId());
        assertEquals("john@petwell.com", retrievedEmployee.getEmail());
    }

    @Test
    void should_ReturnListOfAllSavedEmployees_When_RepositoryIsNotEmpty() {
        // Arrange
        Employee e1 = Employee.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN_TECHNICIAN)
                .role(ADMIN)
                .build();

        Employee e2 = Employee.builder()
                .firstname("Mary")
                .lastname("Smith")
                .email("mary@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN_TECHNICIAN)
                .role(ADMIN)
                .build();

        // Act
        employeeRepository.saveAll(List.of(e1, e2));
        List<Employee> employees = employeeRepository.findAll();

        // Assert
        assertTrue(employees.containsAll(List.of(e1, e2)));
        assertEquals(2, employees.size());
    }

    @Test
    void should_FindEmployeeById_When_ValidEmployeeIDGiven() {
        // Arrange
        Employee e1 = Employee.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN_TECHNICIAN)
                .role(ADMIN)
                .build();

        // Act
        employeeRepository.save(e1);
        Employee foundEmployee = employeeRepository.findById(e1.getId()).orElse(null);

        // Assert
        assertNotNull(foundEmployee);
        assertEquals(e1.getFirstname(), foundEmployee.getFirstname());
        assertEquals(e1.getLastname(), foundEmployee.getLastname());
        assertEquals(e1.getEmail(), foundEmployee.getEmail());
    }

    @Test
    void should_FindEmployeeByEmail_When_ValidEmployeeEmailGiven() {
        // Arrange
        Employee e1 = Employee.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN_TECHNICIAN)
                .role(ADMIN)
                .build();

        // Act
        Employee savedEmployee = employeeRepository.save(e1);
        Employee foundEmployee = employeeRepository.findByEmail(savedEmployee.getEmail()).orElse(new Employee());

        // Assert
        assertNotNull(foundEmployee);
        assertEquals(e1.getFirstname(), foundEmployee.getFirstname());
        assertEquals(e1.getLastname(), foundEmployee.getLastname());
        assertEquals(e1.getEmail(), foundEmployee.getEmail());
    }

    @Test
    void should_UpdateEmployee_When_ValidIDAndEmployeeDTOGiven() {
        // Arrange
        Employee e1 = Employee.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN_TECHNICIAN)
                .role(ADMIN)
                .build();

        Employee updatedEmp = Employee.builder()
                .firstname("Brandon")
                .lastname("Bryan")
                .email("john@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN_TECHNICIAN)
                .role(ADMIN)
                .build();

        // Act
        Employee savedEmployee = employeeRepository.save(e1);
        Employee existingEmployee = employeeRepository.findById(savedEmployee.getId()).orElse(new Employee());
        updatedEmp.setId(existingEmployee.getId());
        Employee updatedEmployee = employeeRepository.save(updatedEmp);

        // Assert
        assertNotNull(existingEmployee);
        assertNotNull(updatedEmployee);
        assertEquals(savedEmployee.getId(), updatedEmployee.getId());
        assertEquals(savedEmployee.getEmail(), updatedEmployee.getEmail());
    }

    @Test
    void should_DeleteEmployee_When_ValidIDGiven() {
        // Arrange
        Employee e1 = Employee.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN_TECHNICIAN)
                .role(ADMIN)
                .build();

        // Act
        Employee savedEmployee = employeeRepository.save(e1);
        employeeRepository.deleteById(savedEmployee.getId());
        List<Employee> employees = employeeRepository.findAll();

        // Assert
        assertTrue(employees.isEmpty());
    }

    @Test
    void should_ReturnTrue_When_EmployeeEmailExistsInDatabase() {
        // Arrange
        Employee e1 = Employee.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN_TECHNICIAN)
                .role(ADMIN)
                .build();

        // Act
        employeeRepository.save(e1);
        boolean isEmailPresent = employeeRepository.existsByEmail("john@petwell.com");

        // Assert
        assertTrue(isEmailPresent);
        assertEquals(1, employeeRepository.count());
    }

    @Test
    void should_ReturnFalse_When_EmployeeEmailDoesNotExistInDatabase() {
        // Arrange
        String nonExistingEmail = "brandon@petwell.com";

        // Act
        boolean isEmailPresent = employeeRepository.existsByEmail(nonExistingEmail);

        // Assert
        assertFalse(isEmailPresent);
    }
}