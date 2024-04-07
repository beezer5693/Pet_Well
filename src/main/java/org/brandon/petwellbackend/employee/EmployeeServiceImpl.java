package org.brandon.petwellbackend.employee;

import lombok.RequiredArgsConstructor;
import org.brandon.petwellbackend.common.Mapper;
import org.brandon.petwellbackend.common.Role;
import org.brandon.petwellbackend.common.User;
import org.brandon.petwellbackend.exceptions.ApplicationException;
import org.brandon.petwellbackend.exceptions.EntityAlreadyExistsException;
import org.brandon.petwellbackend.exceptions.EntityNotFoundException;
import org.brandon.petwellbackend.payload.APIResponse;
import org.brandon.petwellbackend.payload.AuthResponse;
import org.brandon.petwellbackend.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.util.Comparator.comparing;

@Service
@Transactional
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final static Logger LOGGER = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRepository employeeRepository;
    private final AuthenticationManager authenticationManager;
    private final Mapper mapper;
    private final JwtService jwtService;

    /**
     * Registers a new employee using the provided registration details.
     *
     * @param registrationRequest A data transfer object carrying the employee registration details.
     * @return APIResponse<AuthResponseDTO> Returns a new APIResponse object with the generated access token.
     * @throws EntityAlreadyExistsException if the provided employee email is already registered.
     * @throws ApplicationException         if any issues occur while accessing the database.
     */
    @Override
    public APIResponse<AuthResponse> registerEmployee(EmployeeRegistrationRequest registrationRequest) {
        LOGGER.debug("Attempting to register employee: {}", registrationRequest);

        String registrationEmail = registrationRequest.email();

        if (isEmailAlreadyRegistered(registrationEmail)) {
            throwEntityAlreadyExistsException(registrationEmail);
        }

        Employee newEmployee;

        try {
            Employee employee = mapper.mapToEmployee(registrationRequest);
            newEmployee = employeeRepository.save(employee);
        } catch (DataAccessException e) {
            LOGGER.error("An error occured trying to access the database", e);
            throw e;
        }

        String accessToken = generateAccessToken(newEmployee);

        return APIResponse.created(new AuthResponse(accessToken));
    }

    /**
     * Authenticates an employee with the provided login details.
     *
     * @param employeeLoginRequest A data transfer object carrying the employee login details.
     * @return APIResponse<AuthResponseDTO> Returns a new APIResponse object with the generated access token
     * upon successfully validating the credentials.
     * @throws BadCredentialsException if the credentials provided are invalid.
     * @throws ApplicationException    if any issues occur while accessing the database.
     */
    @Override
    public APIResponse<AuthResponse> loginEmployee(EmployeeLoginRequest employeeLoginRequest) {
        LOGGER.debug("Attempting to authenticate employee: {}", employeeLoginRequest);

        String loginEmail = employeeLoginRequest.email();

        Employee existingEmployee;

        try {
            existingEmployee = employeeRepository.findByEmail(loginEmail)
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

            validateLoginCredentials(employeeLoginRequest);
        } catch (BadCredentialsException e) {
            LOGGER.error("Failed to validate login request", e);
            throw e;
        }

        String accessToken = generateAccessToken(existingEmployee);

        return APIResponse.ok(new AuthResponse(accessToken));
    }

    /**
     * Retrieves all employees.
     *
     * @return APIResponse with a list of EmployeeDTO for all employees.
     * @throws ApplicationException if any issues occur while accessing the database.
     */
    @Override
    public APIResponse<List<EmployeeDTO>> getAllEmployees() {
        LOGGER.debug("Attempting to find all employees");

        List<EmployeeDTO> employeeDTOs;

        try {
            employeeDTOs = employeeRepository.findAll()
                    .stream()
                    .sorted(comparing(Employee::getLastname))
                    .map(mapper::mapToDTO)
                    .toList();
        } catch (DataAccessException e) {
            LOGGER.error("An error occured trying to access the database", e);
            throw e;
        }

        if (employeeDTOs.isEmpty()) {
            LOGGER.warn("EmployeeDTO list is empty.");
            throw new ApplicationException(HttpStatus.NOT_FOUND, "Employee list is empty");
        }

        LOGGER.info("EmployeeDTO contains {} employees", employeeDTOs.size());

        return APIResponse.ok(employeeDTOs);
    }

    /**
     * Retrieves an employee's details by the provided ID.
     *
     * @param id The ID of the employee to be retrieved.
     * @return APIResponse with the EmployeeDTO of the found employee.
     * @throws EntityNotFoundException if an employee with the provided ID is not found.
     * @throws ApplicationException    if any issues occur while accessing the database.
     */
    @Override
    public APIResponse<EmployeeDTO> getEmployeeById(Long id) {
        LOGGER.debug("Attempting to find employee by ID: {}", id);

        EmployeeDTO employeeDTO;

        try {
            employeeDTO = employeeRepository.findById(id)
                    .map(mapper::mapToDTO)
                    .orElseThrow(() -> entityNotFoundException(id));
        } catch (DataAccessException e) {
            LOGGER.error("An error occured trying to access the database", e);
            throw e;
        }

        return APIResponse.ok(employeeDTO);
    }

    /**
     * Updates an existing employee's details using the provided EmployeeDTO.
     *
     * @param id          The ID of the employee to be updated.
     * @param employeeDto The new details for the employee.
     * @return APIResponse indicating successful operation.
     * @throws EntityNotFoundException if an employee with the provided ID is not found.
     * @throws ApplicationException    if any issues occur while accessing the database.
     */
    @Override
    public APIResponse<EmployeeDTO> updateEmployee(Long id, EmployeeDTO employeeDto) {
        LOGGER.info("Attempting to update employee with ID: {}", id);

        EmployeeDTO employeeDTO;

        try {
            employeeDTO = employeeRepository.findById(id)
                    .map(employee -> updateEmployeeWithDTOValues(employeeDto, employee))
                    .map(employeeRepository::save)
                    .map(mapper::mapToDTO)
                    .orElseThrow(() -> entityNotFoundException(id));
        } catch (DataAccessException e) {
            LOGGER.error("An error occured trying to access the database", e);
            throw e;
        }

        return APIResponse.ok(employeeDTO);
    }

    /**
     * Deletes an employee identified by the provided ID.
     *
     * @param id The ID of the employee to be deleted.
     * @return APIResponse indicating successful operation.
     * @throws EntityNotFoundException if an employee with the provided ID is not found.
     * @throws ApplicationException    if an issue occurs while accessing the database.
     */
    @Override
    public APIResponse<?> deleteEmployee(Long id) {
        LOGGER.debug("Attempting to delete employee with ID: {}", id);

        try {
            Employee existingEmployee = employeeRepository.findById(id)
                    .orElseThrow(() -> entityNotFoundException(id));

            employeeRepository.deleteById(existingEmployee.getId());
        } catch (DataAccessException e) {
            LOGGER.error("An error occured trying to access the database", e);
            throw e;
        }

        return APIResponse.ok(null);
    }

    private Employee updateEmployeeWithDTOValues(EmployeeDTO employeeDTO, Employee employee) {
        return Employee.builder()
                .id(employee.getId())
                .firstname(getUpdatedStringValue(employeeDTO.firstname(), employee.getFirstname()))
                .lastname(getUpdatedStringValue(employeeDTO.lastname(), employee.getLastname()))
                .email(employee.getEmail())
                .password(employee.getPassword())
                .jobTitle(getUpdatedEnumValue(employeeDTO.jobTitle(), employee.getJobTitle(), JobTitle.class))
                .role(getUpdatedEnumValue(employeeDTO.role(), employee.getRole(), Role.class))
                .build();
    }

    private static String getUpdatedStringValue(String input, String defaultValue) {
        return input != null ? input : defaultValue;
    }

    private <E extends Enum<E>> E getUpdatedEnumValue(String newValue, E oldValue, Class<E> enumType) {
        return newValue != null ? Enum.valueOf(enumType, newValue.toUpperCase()) : oldValue;
    }

    private static EntityNotFoundException entityNotFoundException(Long id) {
        String errorMessage = String.format("Could not find employee with ID: %d", id);
        EntityNotFoundException ex = new EntityNotFoundException(errorMessage, id);
        LOGGER.warn(errorMessage, ex);
        return ex;
    }

    private String generateAccessToken(Employee employee) {
        return jwtService.generateJwtToken(employee);
    }

    private boolean isEmailAlreadyRegistered(String email) {
        try {
            return employeeRepository.existsByEmail(email);
        } catch (DataAccessException e) {
            LOGGER.error("An error occurred trying to access the database", e);
            throw e;
        }
    }

    private static void throwEntityAlreadyExistsException(String registrationEmail) {
        String errorMessage = String.format("%s is already associated with another employee", registrationEmail);
        LOGGER.warn(errorMessage);
        throw new EntityAlreadyExistsException(errorMessage, registrationEmail);
    }

    /**
     * Validates the given login credentials.
     * This method uses the AuthenticationManager to authenticate the provided credentials.
     * If the credentials are not valid, it will throw a BadCredentialsException.
     *
     * @param employeeLoginRequest the data transfer object carrying the user's login credentials.
     * @throws BadCredentialsException if the authentication fails.
     */
    private void validateLoginCredentials(EmployeeLoginRequest employeeLoginRequest) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                employeeLoginRequest.email(),
                employeeLoginRequest.password())
        );
    }
}
