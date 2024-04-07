package org.brandon.petwellbackend.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.brandon.petwellbackend.employee.Employee;
import org.brandon.petwellbackend.employee.EmployeeDetailsService;
import org.brandon.petwellbackend.employee.EmployeeRepository;
import org.brandon.petwellbackend.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.brandon.petwellbackend.common.Role.ADMIN;
import static org.brandon.petwellbackend.common.Role.MANAGER;
import static org.brandon.petwellbackend.employee.JobTitle.VETERINARIAN_TECHNICIAN;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseControllerTestConfiguration extends TestContainerConfiguration {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JwtService jwtService;

    @Autowired
    protected EmployeeRepository employeeRepository;

    @MockBean
    protected EmployeeDetailsService employeeDetailsService;

    protected Employee mockAdmin;

    protected Employee mockManager;

    protected String mockAdminToken;

    protected String mockManagerToken;

    protected ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void beforeEach() {

        mockAdmin = Employee.builder()
                .firstname("Brandon")
                .lastname("Bryan")
                .email("brandon@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN_TECHNICIAN)
                .role(ADMIN)
                .build();

        mockManager = Employee.builder()
                .firstname("Arantxa")
                .lastname("Leon")
                .email("arantxa@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN_TECHNICIAN)
                .role(MANAGER)
                .build();

        employeeRepository.saveAll(List.of(mockAdmin, mockManager));

        mockAdminToken = generateMockToken(mockAdmin);
        mockManagerToken = generateMockToken(mockManager);

        when(employeeDetailsService.loadUserByUsername(mockAdmin.getEmail())).thenReturn(mockAdmin);
        when(employeeDetailsService.loadUserByUsername(mockManager.getEmail())).thenReturn(mockManager);
    }

    @AfterEach
    void afterEach() {
        employeeRepository.deleteAll();
    }

    private String generateMockToken(UserDetails userDetails) {
        return "Bearer ".concat(jwtService.generateJwtToken(userDetails));
    }
}
