package org.brandon.petwellbackend.repository;

import org.brandon.petwellbackend.entity.Role;
import org.brandon.petwellbackend.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.brandon.petwellbackend.enums.RoleType.ADMIN;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserEntityRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest");

    @Autowired
    private UserEntityRepository userEntityRepository;

    @Test
    void shouldEstablishConnectionToPostgresContainer() {
        assertTrue(postgreSQLContainer.isCreated());
        assertTrue(postgreSQLContainer.isRunning());
    }

    @Test
    void should_FindNoEmployees_When_RepositoryIsEmpty() {
        // Arrange & Act
        List<UserEntity> userEntityEntities = userEntityRepository.findAll();

        // Assert
        assertThat(userEntityEntities).withFailMessage("The repository should be empty but it isn't.").isEmpty();
    }

    @Test
    void should_SaveEmployee_When_ValidEmployeeGiven() {
        // Arrange
        UserEntity e1 = UserEntity.builder()
                .userID(UUID.randomUUID().toString())
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password")
                .role(Role.builder().roleType(ADMIN).build())
                .build();

        // Act
        UserEntity savedUserEntity = userEntityRepository.save(e1);
        Long savedEmployeeID = savedUserEntity.getId();
        UserEntity retrievedUserEntity = userEntityRepository.findById(savedEmployeeID).orElse(null);

        // Assert
        assertNotNull(retrievedUserEntity);
        assertEquals(savedEmployeeID, retrievedUserEntity.getId());
        assertEquals("john@petwell.com", retrievedUserEntity.getEmail());
    }

    @Test
    void should_ReturnListOfAllSavedEmployees_When_RepositoryIsNotEmpty() {
        // Arrange
        UserEntity e1 = UserEntity.builder()
                .userID(UUID.randomUUID().toString())
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password")
                .role(Role.builder().roleType(ADMIN).build())
                .build();

        UserEntity e2 = UserEntity.builder()
                .userID(UUID.randomUUID().toString())
                .firstname("Mary")
                .lastname("Smith")
                .email("mary@petwell.com")
                .password("password")
                .role(Role.builder().roleType(ADMIN).build())
                .build();

        // Act
        userEntityRepository.saveAll(List.of(e1, e2));
        List<UserEntity> userEntityEntities = userEntityRepository.findAll();

        // Assert
        assertTrue(userEntityEntities.containsAll(List.of(e1, e2)));
        assertEquals(2, userEntityEntities.size());
    }

    @Test
    void should_FindEmployeeById_When_ValidEmployeeIDGiven() {
        // Arrange
        UserEntity e1 = UserEntity.builder()
                .userID(UUID.randomUUID().toString())
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password")
                .role(Role.builder().roleType(ADMIN).build())
                .build();

        // Act
        userEntityRepository.save(e1);
        UserEntity foundUserEntity = userEntityRepository.findById(e1.getId()).orElse(null);

        // Assert
        assertNotNull(foundUserEntity);
        assertEquals(e1.getFirstname(), foundUserEntity.getFirstname());
        assertEquals(e1.getLastname(), foundUserEntity.getLastname());
        assertEquals(e1.getEmail(), foundUserEntity.getEmail());
    }

    @Test
    void should_FindEmployeeByEmail_When_ValidEmployeeEmailGiven() {
        // Arrange
        UserEntity e1 = UserEntity.builder()
                .userID(UUID.randomUUID().toString())
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password")
                .role(Role.builder().roleType(ADMIN).build())
                .build();

        // Act
        UserEntity savedUserEntity = userEntityRepository.save(e1);
        UserEntity foundUserEntity = userEntityRepository.findByEmail(savedUserEntity.getEmail()).orElse(new UserEntity());

        // Assert
        assertNotNull(foundUserEntity);
        assertEquals(e1.getFirstname(), foundUserEntity.getFirstname());
        assertEquals(e1.getLastname(), foundUserEntity.getLastname());
        assertEquals(e1.getEmail(), foundUserEntity.getEmail());
    }

    @Test
    void should_UpdateEmployee_When_ValidIDAndEmployeeDTOGiven() {
        // Arrange
        UserEntity e1 = UserEntity.builder()
                .userID(UUID.randomUUID().toString())
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password")
                .role(Role.builder().roleType(ADMIN).build())
                .build();

        UserEntity updatedEmp = UserEntity.builder()
                .userID(e1.getUserID())
                .firstname("Brandon")
                .lastname("Bryan")
                .email("john@petwell.com")
                .password("password")
                .role(Role.builder().roleType(ADMIN).build())
                .build();

        // Act
        UserEntity savedUserEntity = userEntityRepository.save(e1);
        UserEntity existingUserEntity = userEntityRepository.findById(savedUserEntity.getId()).orElse(new UserEntity());
        updatedEmp.setId(existingUserEntity.getId());
        UserEntity updatedUserEntity = userEntityRepository.save(updatedEmp);

        // Assert
        assertNotNull(existingUserEntity);
        assertNotNull(updatedUserEntity);
        assertEquals(savedUserEntity.getId(), updatedUserEntity.getId());
        assertEquals(savedUserEntity.getEmail(), updatedUserEntity.getEmail());
    }

    @Test
    void should_DeleteEmployee_When_ValidIDGiven() {
        // Arrange
        UserEntity e1 = UserEntity.builder()
                .userID(UUID.randomUUID().toString())
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password")
                .role(Role.builder().roleType(ADMIN).build())
                .build();

        // Act
        UserEntity savedUserEntity = userEntityRepository.save(e1);
        userEntityRepository.deleteById(savedUserEntity.getId());
        List<UserEntity> userEntityEntities = userEntityRepository.findAll();

        // Assert
        assertTrue(userEntityEntities.isEmpty());
    }

    @Test
    void should_ReturnTrue_When_EmployeeEmailExistsInDatabase() {
        // Arrange
        UserEntity e1 = UserEntity.builder()
                .userID(UUID.randomUUID().toString())
                .firstname("John")
                .lastname("Doe")
                .email("john@petwell.com")
                .password("password")
                .role(Role.builder().roleType(ADMIN).build())
                .build();

        // Act
        userEntityRepository.save(e1);
        boolean isEmailPresent = userEntityRepository.existsByEmail("john@petwell.com");

        // Assert
        assertTrue(isEmailPresent);
        assertEquals(1, userEntityRepository.count());
    }

    @Test
    void should_ReturnFalse_When_EmployeeEmailDoesNotExistInDatabase() {
        // Arrange
        String nonExistingEmail = "brandon@petwell.com";

        // Act
        boolean isEmailPresent = userEntityRepository.existsByEmail(nonExistingEmail);

        // Assert
        assertFalse(isEmailPresent);
    }
}