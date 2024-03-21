package dev.brandon.petwell.employee;

import dev.brandon.petwell.employee.Employee;
import dev.brandon.petwell.employee.EmployeeRepository;
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
import static dev.brandon.petwell.enums.Role.*;
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
        Employee e1 = Employee.builder()
                .firstName("Brandon")
                .lastName("Bryan")
                .email("brandon@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN)
                .role(ADMIN)
                .build();

        entityManager.persist(e1);

        Long savedEmployeeID = e1.getId();
        Employee employee = entityManager.find(Employee.class, savedEmployeeID);

        assertNotNull(employee);
        assertEquals(savedEmployeeID, employee.getId());
        assertEquals("brandon@petwell.com", employee.getEmail());
    }

    @Test
    void Should_Return_A_List_Of_Employees() {
        Employee e1 = Employee.builder()
                .firstName("Brandon")
                .lastName("Bryan")
                .email("brandon@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN)
                .role(ADMIN)
                .build();

        Employee e2 = Employee.builder()
                .firstName("Arantxa")
                .lastName("Leon")
                .email("arantxa@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN)
                .role(ADMIN)
                .build();

        entityManager.persist(e1);
        entityManager.persist(e2);

        List<Employee> employees = employeeRepository.findAll();

        assertEquals(2, employees.size());
        assertTrue(employees.containsAll(List.of(e1, e2)));
    }

    @Test
    void Should_Find_Employee_By_ID() {
        Employee e1 = Employee.builder()
                .firstName("Brandon")
                .lastName("Bryan")
                .email("brandon@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN)
                .role(ADMIN)
                .build();

        Employee e2 = Employee.builder()
                .firstName("Arantxa")
                .lastName("Leon")
                .email("arantxa@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN)
                .role(ADMIN)
                .build();

        entityManager.persist(e1);
        entityManager.persist(e2);

        Employee foundEmployee = entityManager.find(Employee.class, e1.getId());

        assertNotNull(foundEmployee);
        assertEquals(e1.getEmail(), foundEmployee.getEmail());
    }

    @Test
    void Should_Update_Employee_By_ID() {
        Employee e1 = Employee.builder()
                .firstName("Brandon")
                .lastName("Bryan")
                .email("brandon@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN)
                .role(ADMIN)
                .build();

        Employee e2 = Employee.builder()
                .firstName("Arantxa")
                .lastName("Leon")
                .email("arantxa@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN)
                .role(ADMIN)
                .build();

        entityManager.persist(e1);
        entityManager.persist(e2);

        Employee updatedEmployee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@petwell")
                .password("password")
                .jobTitle(VETERINARIAN_TECHNICIAN)
                .build();

        Employee emp = entityManager.find(Employee.class, e2.getId());

        emp.setFirstName(updatedEmployee.getFirstName());
        emp.setLastName(updatedEmployee.getLastName());
        emp.setEmail(updatedEmployee.getEmail());

        entityManager.persist(emp);

        Employee foundEmployee = entityManager.find(Employee.class, e2.getId());

        assertEquals(foundEmployee.getId(), emp.getId());
        assertEquals(foundEmployee.getEmail(), emp.getEmail());
    }

    @Test
    void Should_Delete_Employee_By_ID() {
        Employee e1 = Employee.builder()
                .firstName("Brandon")
                .lastName("Bryan")
                .email("brandon@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN)
                .role(ADMIN)
                .build();

        Employee e2 = Employee.builder()
                .firstName("Arantxa")
                .lastName("Leon")
                .email("arantxa@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN)
                .role(ADMIN)
                .build();

        entityManager.persist(e1);
        entityManager.persist(e2);

        entityManager.remove(e2);

        List<Employee> employees = employeeRepository.findAll();

        assertEquals(1, employees.size());
        assertTrue(employees.contains(e1));
        assertFalse(employees.contains(e2));
    }

    @Test
    void Should_Return_True_If_Email_Exists_In_DB() {
        Employee e1 = Employee.builder()
                .firstName("Brandon")
                .lastName("Bryan")
                .email("brandon@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN)
                .role(ADMIN)
                .build();

        entityManager.persist(e1);

        boolean exists = employeeRepository.existsByEmail("brandon@petwell.com");

        assertTrue(exists);
    }

    @Test
    void Should_Return_False_If_Email_Does_Not_Exist_In_DB() {
        boolean exists = employeeRepository.existsByEmail("brandon@petwell.com");
        assertFalse(exists);
    }
}