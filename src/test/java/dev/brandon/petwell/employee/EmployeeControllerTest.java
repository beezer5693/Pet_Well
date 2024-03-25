package dev.brandon.petwell.employee;

import dev.brandon.petwell.base.BaseControllerTestConfiguration;
import dev.brandon.petwell.exceptions.ApplicationException;
import dev.brandon.petwell.responses.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static dev.brandon.petwell.employee.JobTitle.VETERINARIAN;
import static dev.brandon.petwell.enums.Role.ADMIN;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
class EmployeeControllerTest extends BaseControllerTestConfiguration {

    @MockBean
    private EmployeeServiceImpl employeeServiceImpl;

    private static final String BASE_URL = "/api/v1/employees";

    private EmployeeDTO employeeDto;

    @BeforeEach
    void setUp() {
        employeeDto = new EmployeeDTO(
                "John",
                "Doe",
                "john.doe@example.com",
                "veterinarian",
                "admin"
        );
    }

    @Test
    void Should_Return_A_List_Of_EmployeeDto() throws Exception {
        List<EmployeeDTO> employeeDTOList = List.of(employeeDto);

        when(employeeServiceImpl.getAllEmployees())
                .thenReturn(ApiResponse.successfulResponse("Successful", employeeDTOList));

        ResultActions response = mockMvc.perform(get(BASE_URL).header(HttpHeaders.AUTHORIZATION, mockAdminToken));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.[0].first_name").value(employeeDto.firstName()))
                .andExpect(jsonPath("$.data.[0].last_name").value(employeeDto.lastName()));
    }

    @Test
    void Should_Find_Employee_When_Given_A_Valid_EmployeeID() throws Exception {
        Long id = 1L;

        when(employeeServiceImpl.getEmployeeByID(id))
                .thenReturn(ApiResponse.successfulResponse("Successful", employeeDto));

        ResultActions response = mockMvc.perform(get(BASE_URL + "/{id}", id).header(HttpHeaders.AUTHORIZATION, mockAdminToken));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.email").value(employeeDto.email()));
    }

    @Test
    void Should_Update_Employee_When_Given_A_Valid_EmployeeID_And_EmployeeDto() throws Exception {
        Employee e1 = new Employee(
                1L,
                "John",
                "John",
                "Doe",
                "john.doe@example.com",
                VETERINARIAN,
                ADMIN);

        when(employeeServiceImpl.updateEmployee(e1.getId(), employeeDto))
                .thenReturn(ApiResponse.successfulResponse("Successful", employeeDto));

        ResultActions response = mockMvc.perform(put(BASE_URL + "/{id}", e1.getId())
                .header(HttpHeaders.AUTHORIZATION, mockAdminToken)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDto)));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
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

        ResultActions response = mockMvc.perform(delete(BASE_URL + "/{id}", id).header(HttpHeaders.AUTHORIZATION, mockAdminToken));

        response.andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void Should_Return_StatusCode_404_NOT_FOUND_When_Given_An_Invalid_EmployeeID() throws Exception {
        Long id = 0L;

        when(employeeServiceImpl.getEmployeeByID(id))
                .thenThrow(new ApplicationException(HttpStatus.NOT_FOUND, "Employee Not Found"));

        ResultActions response = mockMvc.perform(get(BASE_URL + "/{id}", id).header(HttpHeaders.AUTHORIZATION, mockAdminToken));

        response.andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(status().is(404));

    }
}