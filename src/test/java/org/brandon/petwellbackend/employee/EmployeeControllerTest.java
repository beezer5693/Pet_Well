package org.brandon.petwellbackend.employee;

import org.brandon.petwellbackend.base.BaseControllerTestConfiguration;
import org.brandon.petwellbackend.exceptions.EntityAlreadyExistsException;
import org.brandon.petwellbackend.exceptions.EntityNotFoundException;
import org.brandon.petwellbackend.payload.APIResponse;
import org.brandon.petwellbackend.payload.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.brandon.petwellbackend.common.Role.ADMIN;
import static org.brandon.petwellbackend.employee.JobTitle.VETERINARIAN_TECHNICIAN;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
class EmployeeControllerTest extends BaseControllerTestConfiguration {

    @MockBean
    private EmployeeService employeeService;

    private static final String BASE_AUTH_URL = "/api/v1/auth/employees";
    private static final String BASE_URL = "/api/v1/employees";

    private EmployeeDTO employeeDTO;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password")
                .jobTitle(VETERINARIAN_TECHNICIAN)
                .role(ADMIN)
                .build();

        employeeDTO = EmployeeDTO.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .jobTitle("veterinarian")
                .role("admin")
                .build();
    }

    @Test
    void should_CreateEmployee_When_ValidRegistrationDetailsProvided() throws Exception {
        // Arrange
        EmployeeRegistrationRequest registrationDTO = EmployeeRegistrationRequest.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john@example.com")
                .password("password123")
                .jobTitle("veterinarian")
                .role("admin")
                .build();

        APIResponse<AuthResponse> expectedResponse = APIResponse.created(new AuthResponse(mockAdminToken));
        String expectedJsonResponse = objectMapper.writeValueAsString(expectedResponse);
        when(employeeService.registerEmployee(registrationDTO)).thenReturn(expectedResponse);

        // Act
        ResultActions response = mockMvc.perform(post(BASE_AUTH_URL.concat("/register"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDTO)));

        // Assert
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().json(expectedJsonResponse));
    }

    @Test
    void should_ReturnConflictStatus_When_RegisteringEmployeeWithExistingEmail() throws Exception {
        // Arrange
        EmployeeRegistrationRequest registrationDTO = EmployeeRegistrationRequest.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john@example.com")
                .password("password123")
                .jobTitle("veterinarian")
                .role("admin")
                .build();


        when(employeeService.registerEmployee(registrationDTO))
                .thenThrow(new EntityAlreadyExistsException("Employee with email " + registrationDTO.email() + " already exists",
                        registrationDTO.email()));

        // Act
        ResultActions response = mockMvc.perform(post(BASE_AUTH_URL.concat("/register"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationDTO)));

        // Assert
        response.andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    void should_LoginEmployee_When_ValidEmployeeCredentialsGiven() throws Exception {
        // Arrange
        EmployeeLoginRequest credentialsDTO = EmployeeLoginRequest.builder()
                .email("john@example.com")
                .password("password123")
                .build();

        APIResponse<AuthResponse> expectedResponse = APIResponse.ok(new AuthResponse(mockAdminToken));
        String expectedJsonResponse = objectMapper.writeValueAsString(expectedResponse);
        when(employeeService.loginEmployee(credentialsDTO)).thenReturn(expectedResponse);

        // Act
        ResultActions response = mockMvc.perform(post(BASE_AUTH_URL.concat("/login"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentialsDTO)));

        // Assert
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJsonResponse));
    }

    @Test
    void should_ReturnUnauthorizedStatus_When_LoginWithInvalidCredentials() throws Exception {
        // Arrange
        EmployeeLoginRequest credentialsDTO = EmployeeLoginRequest.builder()
                .email("john@example.com")
                .password("wrongpassword")
                .build();

        when(employeeService.loginEmployee(credentialsDTO))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act
        ResultActions response = mockMvc.perform(post(BASE_AUTH_URL.concat("/login"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentialsDTO)));

        // Assert
        response.andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void should_ReturnListOfEmployeeDTOs_When_AllEmployeesRequested() throws Exception {
        List<EmployeeDTO> employeeDTOs = List.of(employeeDTO);

        when(employeeService.getAllEmployees())
                .thenReturn(APIResponse.ok(employeeDTOs));

        ResultActions response = mockMvc.perform(get(BASE_URL).header(HttpHeaders.AUTHORIZATION, mockAdminToken));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.[0].first_name").value(employeeDTO.firstname()))
                .andExpect(jsonPath("$.data.[0].last_name").value(employeeDTO.lastname()))
                .andExpect(jsonPath("$.data.[0].email").value(employeeDTO.email()))
                .andExpect(jsonPath("$.data.[0].job_title").value(employeeDTO.jobTitle()));
    }

    @Test
    void should_ReturnEmployeeDTO_When_GivenValidEmployeeID() throws Exception {
        Long id = 1L;

        when(employeeService.getEmployeeById(id))
                .thenReturn(APIResponse.ok(employeeDTO));

        ResultActions response = mockMvc.perform(get(BASE_URL + "/{id}", id).header(HttpHeaders.AUTHORIZATION, mockAdminToken));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.first_name").value(employeeDTO.firstname()))
                .andExpect(jsonPath("$.data.last_name").value(employeeDTO.lastname()))
                .andExpect(jsonPath("$.data.email").value(employeeDTO.email()))
                .andExpect(jsonPath("$.data.job_title").value(employeeDTO.jobTitle()));
    }

    @Test
    void should_UpdateEmployee_When_GivenValidEmployeeIdAndEmployeeDto() throws Exception {
        employee.setId(1L);

        when(employeeService.updateEmployee(employee.getId(), employeeDTO)).thenReturn(APIResponse.ok(null));

        ResultActions response = mockMvc.perform(put(BASE_URL + "/{id}", employee.getId())
                .header(HttpHeaders.AUTHORIZATION, mockAdminToken)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDTO)));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    void should_DeleteEmployee_When_GivenValidEmployeeId() throws Exception {
        Long id = 1L;

        when(employeeService.deleteEmployee(id)).thenReturn(APIResponse.ok(null));

        ResultActions response = mockMvc.perform(delete(BASE_URL + "/{id}", id).header(HttpHeaders.AUTHORIZATION, mockAdminToken));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    void should_ReturnNotFoundStatus_When_GivenInvalidEmployeeId() throws Exception {
        Long invalidId = 0L;

        when(employeeService.getEmployeeById(invalidId))
                .thenThrow(new EntityNotFoundException(String.format("Failed to retrieve employee with ID: %d", invalidId), invalidId));

        ResultActions response = mockMvc.perform(get(BASE_URL + "/{id}", invalidId).header(HttpHeaders.AUTHORIZATION, mockAdminToken));

        response.andDo(print())
                .andExpect(status().isNotFound());
    }
}