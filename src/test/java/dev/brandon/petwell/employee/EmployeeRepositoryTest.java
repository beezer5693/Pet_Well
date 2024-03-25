package dev.brandon.petwell.employee;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static dev.brandon.petwell.employee.JobTitle.VETERINARIAN;
import static dev.brandon.petwell.employee.JobTitle.VETERINARIAN_TECHNICIAN;
import static dev.brandon.petwell.enums.Role.ADMIN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class EmployeeRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void Connection_To_PostgresContainer_Established() {
        assertTrue(postgreSQLContainer.isCreated());
        assertTrue(postgreSQLContainer.isRunning());
    }

    @Test
    void Should_Find_No_Employees_If_Repository_Is_Empty() {
        List<Employee> employees = employeeRepository.findAll();
        assertThat(employees).isEmpty();
    }

    @Test
    void Should_Save_An_Employee() {
        Employee e1 = new Employee(
                "Brandon",
                "Bryan",
                "brandon@petwell.com",
                "password",
                VETERINARIAN,
                ADMIN);

        employeeRepository.save(e1);

        Long savedEmployeeID = e1.getId();

        Employee employee = employeeRepository.findById(savedEmployeeID).orElse(null);

        assertNotNull(employee);
        assertEquals(savedEmployeeID, employee.getId());
        assertEquals("brandon@petwell.com", employee.getEmail());
    }

    @Test
    void Should_Return_A_List_Of_Employees() {
        Employee e1 = new Employee(
                "Brandon",
                "Bryan",
                "brandon@petwell.com",
                "password",
                VETERINARIAN,
                ADMIN);

        Employee e2 = new Employee(
                "Arantxa",
                "Leon",
                "arantxa@petwell.com",
                "password",
                VETERINARIAN,
                ADMIN);

        employeeRepository.saveAll(List.of(e1, e2));

        List<Employee> employees = employeeRepository.findAll();

        assertEquals(2, employees.size());
        assertTrue(employees.containsAll(List.of(e1, e2)));
    }

    @Test
    void Should_Find_Employee_By_ID() {
        Employee e1 = new Employee(
                "Brandon",
                "Bryan",
                "brandon@petwell.com",
                "password",
                VETERINARIAN,
                ADMIN);

        Employee e2 = new Employee(
                "Arantxa",
                "Leon",
                "arantxa@petwell.com",
                "password",
                VETERINARIAN,
                ADMIN);

        employeeRepository.saveAll(List.of(e1, e2));

        Employee foundEmployee = employeeRepository.findById(e1.getId()).orElse(null);

        assertNotNull(foundEmployee);
        assertEquals(e1.getFirstName(), foundEmployee.getFirstName());
        assertEquals(e1.getEmail(), foundEmployee.getEmail());
    }

    @Test
    void Should_Update_Employee_By_ID() {
        Employee e1 = new Employee(
                "Brandon",
                "Bryan",
                "brandon@petwell.com",
                "password",
                VETERINARIAN,
                ADMIN);

        Employee e2 = new Employee(
                "Arantxa",
                "Leon",
                "arantxa@petwell.com",
                "password",
                VETERINARIAN,
                ADMIN);

        employeeRepository.saveAll(List.of(e1, e2));

        Employee updatedEmployee = new Employee(
                "John",
                "Doe",
                "john@petwell.com",
                "password",
                VETERINARIAN_TECHNICIAN,
                ADMIN);

        Employee existingEmployee = employeeRepository.findById(e2.getId()).orElse(null);

        assert existingEmployee != null;

        Employee employee = new Employee(
                existingEmployee.getId(),
                updatedEmployee.getFirstName(),
                updatedEmployee.getLastName(),
                updatedEmployee.getEmail(),
                existingEmployee.getPassword(),
                existingEmployee.getJobTitle(),
                existingEmployee.getRole());

        employeeRepository.save(employee);

        Employee foundEmployee = employeeRepository.findById(e2.getId()).orElse(null);

        assert foundEmployee != null;

        assertEquals(foundEmployee.getId(), existingEmployee.getId());
        assertEquals(foundEmployee.getEmail(), existingEmployee.getEmail());
    }

    @Test
    void Should_Delete_Employee_By_ID() {
        Employee e1 = new Employee(
                "Brandon",
                "Bryan",
                "brandon@petwell.com",
                "password",
                VETERINARIAN,
                ADMIN);

        Employee e2 = new Employee(
                "Arantxa",
                "Leon",
                "arantxa@petwell.com",
                "password",
                VETERINARIAN,
                ADMIN);

        employeeRepository.saveAll(List.of(e1, e2));

        employeeRepository.delete(e2);

        List<Employee> employees = employeeRepository.findAll();

        assertEquals(1, employees.size());
        assertTrue(employees.contains(e1));
        assertFalse(employees.contains(e2));
    }

    @Test
    void Should_Return_True_If_Email_Exists_In_DB() {
        Employee e1 = new Employee(
                "Brandon",
                "Bryan",
                "brandon@petwell.com",
                "password",
                VETERINARIAN,
                ADMIN);

        employeeRepository.save(e1);

        boolean exists = employeeRepository.existsByEmail("brandon@petwell.com");

        assertTrue(exists);
    }

    @Test
    void Should_Return_False_If_Email_Does_Not_Exist_In_DB() {
        boolean exists = employeeRepository.existsByEmail("brandon@petwell.com");
        assertFalse(exists);
    }
}