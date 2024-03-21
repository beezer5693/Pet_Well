package dev.brandon.petwell.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.brandon.petwell.config.SecurityConfiguration;
import dev.brandon.petwell.employee.EmployeeController;
import dev.brandon.petwell.employee.EmployeeDto;
import dev.brandon.petwell.employee.NewEmployeeRequest;
import dev.brandon.petwell.exceptions.ApplicationException;
import dev.brandon.petwell.employee.Employee;
import dev.brandon.petwell.responses.ApiResponse;
import dev.brandon.petwell.employee.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static dev.brandon.petwell.employee.JobTitle.VETERINARIAN;
import static dev.brandon.petwell.enums.Role.ADMIN;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfiguration.class)
@WebMvcTest(EmployeeController.class)
@Testcontainers
class EmployeeControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private EmployeeServiceImpl employeeServiceImpl;

    private static final String BASE_URL = "/api/v1/employees";

    private Employee employee;
    private EmployeeDto employeeDto;
    private NewEmployeeRequest request;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .jobTitle(VETERINARIAN)
                .role(ADMIN)
                .build();

        employeeDto = EmployeeDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .jobTitle("veterinarian")
                .role("admin")
                .build();

        request = NewEmployeeRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("password")
                .jobTitle("veterinarian")
                .role("admin")
                .build();
    }

    @Test
    void Connection_To_PostgresContainer_Established() {
        assertTrue(postgreSQLContainer.isCreated());
        assertTrue(postgreSQLContainer.isRunning());
    }

    @Test
    void Should_Save_Employee_When_Given_A_Valid_Employee() throws Exception {
        when(employeeServiceImpl.saveEmployee(request))
                .thenReturn(ApiResponse.successfulResponse(HttpStatus.CREATED.value(), "Successfull", employeeDto));

        ResultActions response = mockMvc.perform(post(BASE_URL)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)));

        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.first_name").value(employeeDto.firstName()))
                .andExpect(jsonPath("$.data.last_name").value(employeeDto.lastName()))
                .andExpect(jsonPath("$.data.email").value(employeeDto.email()))
                .andExpect(jsonPath("$.data.job_title").value(employeeDto.jobTitle()));
    }

    @Test
    void Should_Return_A_List_Of_EmployeeDto() throws Exception {
        List<EmployeeDto> employeeDtoList = List.of(employeeDto);

        when(employeeServiceImpl.findAllEmployees())
                .thenReturn(ApiResponse.successfulResponse("Successful", employeeDtoList));

        ResultActions response = mockMvc.perform(get(BASE_URL));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.[0].first_name").value(employeeDto.firstName()))
                .andExpect(jsonPath("$.data.[0].last_name").value(employeeDto.lastName()));
    }

    @Test
    void Should_Find_Employee_When_Given_A_Valid_EmployeeID() throws Exception {
        Long id = 1L;

        when(employeeServiceImpl.findEmployeeByID(id))
                .thenReturn(ApiResponse.successfulResponse("Successful", employeeDto));

        ResultActions response = mockMvc.perform(get(BASE_URL + "/{id}", id));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(employeeDto.email()));
    }

    @Test
    void Should_Update_Employee_When_Given_A_Valid_EmployeeID_And_EmployeeDto() throws Exception {
        when(employeeServiceImpl.updateEmployee(employee.getId(), employeeDto))
                .thenReturn(ApiResponse.successfulResponse("Successful", employeeDto));

        ResultActions response = mockMvc.perform(put(BASE_URL + "/{id}", employee.getId())
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(employeeDto)));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.first_name").value(employeeDto.firstName()))
                .andExpect(jsonPath("$.data.last_name").value(employeeDto.lastName()))
                .andExpect(jsonPath("$.data.email").value(employeeDto.email()))
                .andExpect(jsonPath("$.data.job_title").value(employeeDto.jobTitle()));
    }

    @Test
    void Should_Delete_Employee_When_Given_A_Valid_EmployeeID() throws Exception {
        Long id = 1L;

        when(employeeServiceImpl.deleteEmployee(id))
                .thenReturn(ApiResponse.successfulResponse("Successful", id));

        ResultActions response = mockMvc.perform(delete(BASE_URL + "/{id}", id));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(id));
    }

    @Test
    void Should_Return_StatusCode_404_NOT_FOUND_When_Given_An_Invalid_EmployeeID() throws Exception {
        Long id = 0L;

        when(employeeServiceImpl.findEmployeeByID(id))
                .thenThrow(new ApplicationException(HttpStatus.NOT_FOUND, "Employee Not Found", null));

        ResultActions response = mockMvc.perform(get(BASE_URL + "/{id}", id));

        response.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(status().is(404));

    }

    @Test
    void Should_Return_StatusCode_409_CONFLICT_When_Given_An_Already_Existing_Employee_Email() throws Exception {
        when(employeeServiceImpl.saveEmployee(request)).thenThrow(new ApplicationException(HttpStatus.CONFLICT, "Employee Already Exists", null));

        ResultActions response = mockMvc.perform(post(BASE_URL)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)));

        response.andDo(print())
                .andExpect(status().isConflict())
                .andExpect(status().is(409));
    }

    @Test
    void Should_Return_StatusCode_400_BAD_REQUEST_When_Given_An_Employee_With_A_Null_Empty_Or_Blank_Field() throws Exception {
        var request = NewEmployeeRequest.builder()
                .firstName("")
                .lastName("Doe")
                .email("brandon@petwell.com")
                .password("password")
                .jobTitle("veterinarian")
                .role("manager")
                .build();

        ResultActions response = mockMvc.perform(post(BASE_URL)
                .contentType(APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)));

        response.andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(status().is(400));
    }
}