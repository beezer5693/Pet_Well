package org.brandon.petwellbackend.service.impl;

import lombok.RequiredArgsConstructor;
import org.brandon.petwellbackend.common.Mapper;
import org.brandon.petwellbackend.entity.UserEntity;
import org.brandon.petwellbackend.entity.Role;
import org.brandon.petwellbackend.enums.RoleType;
import org.brandon.petwellbackend.exception.ApplicationException;
import org.brandon.petwellbackend.exception.EntityAlreadyExistsException;
import org.brandon.petwellbackend.exception.EntityNotFoundException;
import org.brandon.petwellbackend.payload.UserDTO;
import org.brandon.petwellbackend.payload.UserRegistrationRequest;
import org.brandon.petwellbackend.repository.UserEntityRepository;
import org.brandon.petwellbackend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.BiFunction;

import static java.util.Comparator.comparing;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final static Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserEntityRepository userEntityRepository;
    private final Mapper mapper;

    private static final BiFunction<String, String, String> getUpdatedStringValue = (input, defaultValue) ->
            input != null ? input : defaultValue;

    /**
     * Registers a new user based on the registration request provided.
     *
     * @param registrationRequest The request containing user registration data
     * @return UserEntity representing the registered user
     * @throws EntityAlreadyExistsException if the email provided in the registration request is already registered
     * @throws DataAccessException          in case of any database access related issues
     */
    @Override
    public UserEntity registerUser(UserRegistrationRequest registrationRequest) {
        LOGGER.debug("Attempting to register user: {}", registrationRequest);
        String registrationEmail = registrationRequest.email();
        if (isEmailAlreadyRegistered(registrationEmail)) {
            handleEntityAlreadyExistsException(registrationEmail);
        }
        try {
            UserEntity userEntity = mapper.toUser(registrationRequest);
            return userEntityRepository.save(userEntity);
        } catch (DataAccessException e) {
            LOGGER.error("An error occured trying to access the database", e);
            throw e;
        }
    }

    /**
     * Retrieves all stored users
     *
     * @return List of UserDTO for all employees.
     * @throws ApplicationException if any issues occur while accessing the database.
     */
    @Override
    public List<UserDTO> getAllUsers() {
        LOGGER.debug("Attempting to find all user");
        try {
            List<UserDTO> userDTOS = userEntityRepository.findAll()
                    .stream()
                    .sorted(comparing(UserEntity::getLastname))
                    .map(mapper::toUserDTO)
                    .toList();
            if (userDTOS.isEmpty()) {
                LOGGER.warn("UserDTO list is empty.");
                throw new ApplicationException(HttpStatus.NOT_FOUND, "No users found");
            }
            LOGGER.info("UserDTO contains {} employees", userDTOS.size());
            return userDTOS;
        } catch (DataAccessException e) {
            LOGGER.error("An error occured trying to access the database", e);
            throw e;
        }
    }

    /**
     * Retrieves a user's details by the provided ID.
     *
     * @param userID The ID of the user to be retrieved.
     * @return the UserDTO of the found employee.
     * @throws EntityNotFoundException if a user with the provided ID is not found.
     * @throws ApplicationException    if any issues occur while accessing the database.
     */
    @Override
    public UserDTO getUserByUserID(String userID) {
        LOGGER.debug("Attempting to find user by ID: {}", userID);
        try {
            return userEntityRepository.findByUserID(userID)
                    .map(mapper::toUserDTO)
                    .orElseThrow(() -> handleEntityNotFoundException(userID));
        } catch (DataAccessException e) {
            LOGGER.error("An error occured trying to access the database", e);
            throw e;
        }
    }

    /**
     * Retrieves a user based on the email provided.
     *
     * @param email The email id of the user to be retrieved
     * @return UserEntity object containing all the user details corresponding to the provided email id
     * @throws EntityNotFoundException if no user corresponding to the email is found
     * @throws DataAccessException     in case of any database access related issues
     */
    @Override
    public UserEntity getUserByEmail(String email) {
        LOGGER.debug("Attempting to find user: {}", email);
        try {
            return userEntityRepository.findByEmail(email)
                    .orElseThrow(() -> handleEntityNotFoundException(email));
        } catch (DataAccessException e) {
            LOGGER.error("An error occured trying to access the database", e);
            throw e;
        }
    }

    /**
     * Updates an existing employee's details using the provided EmployeeDTO.
     *
     * @param userID  The ID of the employee to be updated.
     * @param userDto The new details for the employee.
     * @return EmployeeDTO indicating successful operation.
     * @throws EntityNotFoundException if an employee with the provided ID is not found.
     * @throws ApplicationException    if any issues occur while accessing the database.
     */
    @Override
    public UserDTO updateUser(String userID, UserDTO userDto) {
        LOGGER.info("Attempting to update user with ID: {}", userID);
        try {
            return userEntityRepository.findByUserID(userID)
                    .map(user -> updateUserEntity(userDto, user))
                    .map(userEntityRepository::save)
                    .map(mapper::toUserDTO)
                    .orElseThrow(() -> handleEntityNotFoundException(userID));
        } catch (DataAccessException e) {
            LOGGER.error("An error occured trying to access the database", e);
            throw e;
        }
    }

    /**
     * Deletes a user identified by the provided ID.
     *
     * @param userID The ID of the user to be deleted.
     * @throws EntityNotFoundException if a user with the provided ID is not found.
     * @throws ApplicationException    if an issue occurs while accessing the database.
     */
    @Override
    public void deleteUser(String userID) {
        LOGGER.debug("Attempting to delete user with ID: {}", userID);
        try {
            UserEntity userEntityToDelete = userEntityRepository.findByUserID(userID)
                    .orElseThrow(() -> handleEntityNotFoundException(userID));

            userEntityRepository.deleteByUserID(userEntityToDelete.getUserID());
        } catch (DataAccessException e) {
            LOGGER.error("An error occured trying to access the database", e);
            throw e;
        }
    }

    /**
     * Checks if a user is already registered with the provided email.
     *
     * @param email The email address to check for registration
     * @return True if the email is already registered, false otherwise
     * @throws DataAccessException in case of any database access related issues
     */
    @Override
    public boolean isEmailAlreadyRegistered(String email) {
        try {
            return userEntityRepository.existsByEmail(email);
        } catch (DataAccessException e) {
            LOGGER.error("An error occurred trying to access the database", e);
            throw e;
        }
    }

    private UserEntity updateUserEntity(UserDTO userDTO, UserEntity userEntity) {
        return UserEntity.builder()
                .userID(userEntity.getUserID())
                .firstname(getUpdatedStringValue.apply(userDTO.firstname(), userEntity.getFirstname()))
                .lastname(getUpdatedStringValue.apply(userDTO.lastname(), userEntity.getLastname()))
                .email(userEntity.getEmail())
                .password(userEntity.getPassword())
                .role(Role.builder().roleType(getRoleType(userDTO, userEntity)).build())
                .build();
    }

    private static RoleType getRoleType(UserDTO userDTO, UserEntity userEntity) {
        String roleTypeName = getUpdatedStringValue.apply(userDTO.role(), userEntity.getRole().getRoleType().getName());
        return RoleType.valueOf(roleTypeName.toUpperCase());
    }

    private static EntityNotFoundException handleEntityNotFoundException(String userID) {
        String errorMessage = String.format("Could not find user: %s", userID);
        EntityNotFoundException ex = new EntityNotFoundException(errorMessage, userID);
        LOGGER.warn(errorMessage, ex);
        return ex;
    }

    private static void handleEntityAlreadyExistsException(String registrationEmail) {
        String errorMessage = String.format("%s is already associated with another user", registrationEmail);
        EntityAlreadyExistsException ex = new EntityAlreadyExistsException(errorMessage, registrationEmail);
        LOGGER.warn(errorMessage, ex);
        throw ex;
    }
}
