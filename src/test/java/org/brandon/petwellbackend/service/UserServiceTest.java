package org.brandon.petwellbackend.service;

import org.brandon.petwellbackend.common.Mapper;
import org.brandon.petwellbackend.entity.Role;
import org.brandon.petwellbackend.entity.UserEntity;
import org.brandon.petwellbackend.exception.EntityNotFoundException;
import org.brandon.petwellbackend.payload.UserDTO;
import org.brandon.petwellbackend.repository.UserEntityRepository;
import org.brandon.petwellbackend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.brandon.petwellbackend.enums.RoleType.ADMIN;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @Mock
    private UserEntityRepository userEntityRepository;

    @Mock
    private Mapper mapper;

    @InjectMocks
    private UserServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void should_GetEmployee_When_ValidIDGiven() {
        // Arrange
        String userId = UUID.randomUUID().toString();

        UserEntity expectedUserEntity = UserEntity.builder()
                .userID(userId)
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password")
                .role(Role.builder().roleType(ADMIN).build())
                .build();

        UserDTO expectedUserDto = UserDTO.builder()
                .userID(userId)
                .firstname(expectedUserEntity.getFirstname())
                .lastname(expectedUserEntity.getLastname())
                .email(expectedUserEntity.getEmail())
                .role(expectedUserEntity.getRole().getRoleType().name())
                .build();

        when(userEntityRepository.findByUserID(userId)).thenReturn(Optional.of(expectedUserEntity));
        when(mapper.toUserDTO(expectedUserEntity)).thenReturn(expectedUserDto);

        // Act
        UserDTO employeeById = employeeService.getUserByUserID(userId);

        // Assert
        assertEquals(expectedUserDto, employeeById);
        assertEquals(expectedUserDto.firstname(), employeeById.firstname());
        assertEquals(expectedUserDto.lastname(), employeeById.lastname());
        assertEquals(expectedUserDto.email(), employeeById.email());

        verify(userEntityRepository, times(1)).findByUserID(userId);
    }

    @Test
    void should_GetAllEmployees_When_UsersExist() {
        // Arrange
        UserEntity expectedUserEntity1 = UserEntity.builder()
                .id(1L)
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password")
                .role(Role.builder().roleType(ADMIN).build())
                .build();

        UserEntity expectedUserEntity2 = UserEntity.builder()
                .id(2L)
                .firstname("Mary")
                .lastname("Smith")
                .email("mary@petwell.com")
                .password("password")
                .role(Role.builder().roleType(ADMIN).build())
                .build();

        List<UserEntity> expectedUserEntityEntities = List.of(expectedUserEntity1, expectedUserEntity2);

        when(userEntityRepository.findAll()).thenReturn(expectedUserEntityEntities);

        // Act
        List<UserDTO> userDTOList = employeeService.getAllUsers();

        // Assert
        assertFalse(userDTOList.isEmpty());
        assertEquals(expectedUserEntityEntities.size(), userDTOList.size());

        verify(userEntityRepository, times(1)).findAll();
    }

    @Test
    void should_UpdateEmployee_When_ValidIDAndUserDtoGiven() {
        // Arrange
        String idToUpdate = UUID.randomUUID().toString();

        UserEntity existingUserEntity = UserEntity.builder()
                .userID(idToUpdate)
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password")
                .role(Role.builder().roleType(ADMIN).build())
                .build();

        UserDTO updatedEmployeeData = UserDTO.builder()
                .firstname("Brandon")
                .lastname("Bryan")
                .build();

        UserEntity expectedUpdatedUserEntity = UserEntity.builder()
                .userID(idToUpdate)
                .firstname(updatedEmployeeData.firstname())
                .lastname(updatedEmployeeData.lastname())
                .email(existingUserEntity.getEmail())
                .password(existingUserEntity.getPassword())
                .role(existingUserEntity.getRole())
                .build();

        UserDTO expectedUpdatedUserDTO = UserDTO.builder()
                .firstname(expectedUpdatedUserEntity.getFirstname())
                .lastname(expectedUpdatedUserEntity.getLastname())
                .email(expectedUpdatedUserEntity.getEmail())
                .role(expectedUpdatedUserEntity.getRole().getRoleType().getName())
                .build();

        when(userEntityRepository.findByUserID(idToUpdate)).thenReturn(Optional.of(existingUserEntity));
        when(userEntityRepository.save(any(UserEntity.class))).thenReturn(expectedUpdatedUserEntity);
        when(mapper.toUserDTO(expectedUpdatedUserEntity)).thenReturn(expectedUpdatedUserDTO);

        // Act
        UserDTO updatedEmployee = employeeService.updateUser(idToUpdate, updatedEmployeeData);

        // Assert
        assertNotNull(updatedEmployee);
        assertEquals(expectedUpdatedUserDTO, updatedEmployee);

        verify(userEntityRepository, times(1)).findByUserID(idToUpdate);
        verify(userEntityRepository, times(1)).save(any(UserEntity.class));
        verify(mapper, times(1)).toUserDTO(expectedUpdatedUserEntity);
    }

    @Test
    void should_DeleteUser_When_ValidIDGiven() {
        // Arrange
        String idToDelete = UUID.randomUUID().toString();

        UserEntity existingUserEntity = UserEntity.builder()
                .userID(idToDelete)
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password")
                .role(Role.builder().roleType(ADMIN).build())
                .build();

        when(userEntityRepository.findByUserID(idToDelete)).thenReturn(Optional.of(existingUserEntity));
        doNothing().when(userEntityRepository).deleteByUserID(idToDelete);

        // Act
        employeeService.deleteUser(idToDelete);

        // Assert
        verify(userEntityRepository, times(1)).findByUserID(idToDelete);
        verify(userEntityRepository, times(1)).deleteByUserID(idToDelete);
    }

    @Test
    void should_ThrowEntityNotFoundException_When_InvalidIDGiven() {
        // Arrange
        String invalidId = UUID.randomUUID().toString();

        when(userEntityRepository.findByUserID(invalidId)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> employeeService.getUserByUserID(invalidId));

        assertEquals(String.format("Could not find user: %s", invalidId), exception.getMessage());

        verify(userEntityRepository, times(1)).findByUserID(invalidId);
    }
}