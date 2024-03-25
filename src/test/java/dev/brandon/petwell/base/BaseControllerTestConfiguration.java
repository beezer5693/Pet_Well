package dev.brandon.petwell.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.brandon.petwell.employee.Employee;
import dev.brandon.petwell.jwt.JwtService;
import dev.brandon.petwell.security.EmployeeDetails;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static dev.brandon.petwell.employee.JobTitle.VETERINARIAN_TECHNICIAN;
import static dev.brandon.petwell.enums.Role.ADMIN;
import static dev.brandon.petwell.enums.Role.MANAGER;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseControllerTestConfiguration extends TestContainerConfiguration {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtService jwtService;

    @MockBean
    protected UserDetailsService userDetailsService;

    protected Employee mockAdmin;

    protected String mockAdminToken;

    protected Employee mockManager;

    protected String mockManagerToken;

    @BeforeEach
    void initializeAuth() {

        mockAdmin = new Employee(
                "Brandon",
                "Bryan",
                "brandon@example.com",
                "password",
                VETERINARIAN_TECHNICIAN,
                ADMIN);

        mockManager = new Employee(
                "Arantxa",
                "Leon",
                "arantxa@example.com",
                "password",
                VETERINARIAN_TECHNICIAN,
                MANAGER);

        EmployeeDetails mockAdminDetails = new EmployeeDetails(mockAdmin);
        EmployeeDetails mockManagerDetails = new EmployeeDetails(mockManager);

        mockAdminToken = generateMockToken(mockAdminDetails);
        mockManagerToken = generateMockToken(mockManagerDetails);

        when(userDetailsService.loadUserByUsername(mockAdmin.getEmail())).thenReturn(mockAdminDetails);
        when(userDetailsService.loadUserByUsername(mockManager.getEmail())).thenReturn(mockManagerDetails);
    }

    private String generateMockToken(UserDetails userDetails) {
        return "Bearer ".concat(jwtService.generateToken(userDetails));
    }
}
