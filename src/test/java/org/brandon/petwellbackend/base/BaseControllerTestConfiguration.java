package org.brandon.petwellbackend.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.brandon.petwellbackend.entity.Employee;
import org.brandon.petwellbackend.entity.Role;
import org.brandon.petwellbackend.repository.EmployeeRepository;
import org.brandon.petwellbackend.security.EmployeeDetailsService;
import org.brandon.petwellbackend.service.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.brandon.petwellbackend.enums.JobTitle.VETERINARIAN_TECHNICIAN;
import static org.brandon.petwellbackend.enums.RoleType.ADMIN;
import static org.brandon.petwellbackend.enums.RoleType.MANAGER;
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
                .userId(UUID.randomUUID().toString())
                .firstname("Brandon")
                .lastname("Bryan")
                .email("brandon@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN_TECHNICIAN)
                .role(Role.builder().roleType(ADMIN).build())
                .build();

        mockManager = Employee.builder()
                .userId(UUID.randomUUID().toString())
                .firstname("Arantxa")
                .lastname("Leon")
                .email("arantxa@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN_TECHNICIAN)
                .role(Role.builder().roleType(MANAGER).build())
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
