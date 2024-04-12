package org.brandon.petwellbackend.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.brandon.petwellbackend.entity.Role;
import org.brandon.petwellbackend.entity.UserEntity;
import org.brandon.petwellbackend.repository.UserEntityRepository;
import org.brandon.petwellbackend.security.CustomUserDetailsService;
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
    protected UserEntityRepository userEntityRepository;

    @MockBean
    protected CustomUserDetailsService customUserDetailsService;

    protected UserEntity mockAdmin;

    protected UserEntity mockManager;

    protected String mockAdminToken;

    protected String mockManagerToken;

    protected ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void beforeEach() {

        mockAdmin = UserEntity.builder()
                .userID(UUID.randomUUID().toString())
                .firstname("Brandon")
                .lastname("Bryan")
                .email("brandon@petwell.com")
                .password("password")
                .role(Role.builder().roleType(ADMIN).build())
                .build();

        mockManager = UserEntity.builder()
                .userID(UUID.randomUUID().toString())
                .firstname("Arantxa")
                .lastname("Leon")
                .email("arantxa@petwell.com")
                .password("password")
                .role(Role.builder().roleType(MANAGER).build())
                .build();

        userEntityRepository.saveAll(List.of(mockAdmin, mockManager));

        mockAdminToken = generateMockToken(mockAdmin);
        mockManagerToken = generateMockToken(mockManager);

        when(customUserDetailsService.loadUserByUsername(mockAdmin.getEmail())).thenReturn(mockAdmin);
        when(customUserDetailsService.loadUserByUsername(mockManager.getEmail())).thenReturn(mockManager);
    }

    @AfterEach
    void afterEach() {
        userEntityRepository.deleteAll();
    }

    private String generateMockToken(UserDetails userDetails) {
        return "Bearer ".concat(jwtService.generateJwtToken(userDetails));
    }
}
