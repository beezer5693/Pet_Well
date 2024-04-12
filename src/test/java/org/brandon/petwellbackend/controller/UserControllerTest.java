package org.brandon.petwellbackend.controller;

import org.brandon.petwellbackend.base.BaseControllerTestConfiguration;
import org.brandon.petwellbackend.entity.UserEntity;
import org.brandon.petwellbackend.entity.Role;
import org.brandon.petwellbackend.exception.EntityAlreadyExistsException;
import org.brandon.petwellbackend.exception.EntityNotFoundException;
import org.brandon.petwellbackend.payload.UserDTO;
import org.brandon.petwellbackend.payload.UserRegistrationRequest;
import org.brandon.petwellbackend.payload.Response;
import org.brandon.petwellbackend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.UUID;

import static org.brandon.petwellbackend.enums.RoleType.ADMIN;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@DirtiesContext
class UserControllerTest extends BaseControllerTestConfiguration {
    @MockBean
    private UserService userService;

    private static final String BASE_AUTH_URL = "/api/v1/auth/users";
    private static final String BASE_URL = "/api/v1/users";

    private UserDTO userDTO;

    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userEntity = UserEntity.builder()
                .userID(UUID.randomUUID().toString())
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password123")
                .role(Role.builder().roleType(ADMIN).build())
                .build();

        userDTO = UserDTO.builder()
                .userID(userEntity.getUserID())
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .role("Admin")
                .build();
    }

    @Test
    void should_CreateUser_When_ValidRegistrationDetailsProvided() throws Exception {
        // Arrange
        UserRegistrationRequest registrationDTO = UserRegistrationRequest.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password123")
                .build();

        Response<UserDTO> expectedResponse = Response.success(userDTO, HttpStatus.CREATED);
        String expectedJsonResponse = objectMapper.writeValueAsString(expectedResponse);
        when(userService.registerUser(registrationDTO)).thenReturn(userEntity);

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
    void should_ReturnConflictStatus_When_RegisteringUserWithExistingEmail() throws Exception {
        // Arrange
        UserRegistrationRequest registrationDTO = UserRegistrationRequest.builder()
                .firstname("John")
                .lastname("Doe")
                .email("john@example.com")
                .password("password123")
                .build();


        when(userService.registerUser(registrationDTO))
                .thenThrow(new EntityAlreadyExistsException("User with email " + registrationDTO.email() + " already exists",
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
    void should_ReturnListOfUserDTOs_When_AllUsersRequested() throws Exception {
        List<UserDTO> userDTOS = List.of(userDTO);

        when(userService.getAllUsers())
                .thenReturn(userDTOS);

        ResultActions response = mockMvc.perform(get(BASE_URL).header(HttpHeaders.AUTHORIZATION, mockAdminToken));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.[0].first_name").value(userDTO.firstname()))
                .andExpect(jsonPath("$.data.[0].last_name").value(userDTO.lastname()))
                .andExpect(jsonPath("$.data.[0].email").value(userDTO.email()));
    }

    @Test
    void should_ReturnUserDTO_When_GivenValidUserID() throws Exception {
        String id = UUID.randomUUID().toString();

        when(userService.getUserByUserID(id))
                .thenReturn(userDTO);

        ResultActions response = mockMvc.perform(get(BASE_URL + "/{id}", id).header(HttpHeaders.AUTHORIZATION, mockAdminToken));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.data.first_name").value(userDTO.firstname()))
                .andExpect(jsonPath("$.data.last_name").value(userDTO.lastname()))
                .andExpect(jsonPath("$.data.email").value(userDTO.email()));
    }

    @Test
    void should_UpdateUser_When_GivenValidUserIdAndUserDto() throws Exception {
        String id = UUID.randomUUID().toString();

        userEntity.setUserID(id);

        when(userService.updateUser(userEntity.getUserID(), userDTO)).thenReturn(userDTO);

        ResultActions response = mockMvc.perform(put(BASE_URL + "/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, mockAdminToken)
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    void should_DeleteUser_When_GivenValidUserId() throws Exception {
        String id = UUID.randomUUID().toString();

        doNothing().when(userService).deleteUser(id);

        ResultActions response = mockMvc.perform(delete(BASE_URL + "/{id}", id).header(HttpHeaders.AUTHORIZATION, mockAdminToken));

        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    void should_ReturnNotFoundStatus_When_GivenInvalidUserId() throws Exception {
        String invalidId = UUID.randomUUID().toString();

        when(userService.getUserByUserID(invalidId))
                .thenThrow(new EntityNotFoundException(String.format("Could not find employee: %s", invalidId), invalidId));

        ResultActions response = mockMvc.perform(get(BASE_URL + "/{id}", invalidId).header(HttpHeaders.AUTHORIZATION, mockAdminToken));

        response.andDo(print())
                .andExpect(status().isNotFound());
    }
}