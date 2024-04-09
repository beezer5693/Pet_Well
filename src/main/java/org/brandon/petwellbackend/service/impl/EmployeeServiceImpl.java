package org.brandon.petwellbackend.service.impl;

import lombok.RequiredArgsConstructor;
import org.brandon.petwellbackend.common.Mapper;
import org.brandon.petwellbackend.entity.Employee;
import org.brandon.petwellbackend.entity.Role;
import org.brandon.petwellbackend.enums.JobTitle;
import org.brandon.petwellbackend.enums.RoleType;
import org.brandon.petwellbackend.exception.ApplicationException;
import org.brandon.petwellbackend.exception.EntityAlreadyExistsException;
import org.brandon.petwellbackend.exception.EntityNotFoundException;
import org.brandon.petwellbackend.payload.EmployeeDTO;
import org.brandon.petwellbackend.payload.EmployeeLoginRequest;
import org.brandon.petwellbackend.payload.EmployeeRegistrationRequest;
import org.brandon.petwellbackend.repository.EmployeeRepository;
import org.brandon.petwellbackend.service.EmployeeService;
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
import java.util.function.BiFunction;

import static java.util.Comparator.comparing;

@Service
@Transactional
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final static Logger LOGGER = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRepository employeeRepository;
    private final AuthenticationManager authenticationManager;
    private final Mapper mapper;

    private static final BiFunction<String, String, String> getUpdatedStringValue = (input, defaultValue) ->
            input != null ? input : defaultValue;

    /**
     * Registers a new employee using the provided registration details.
     *
     * @param registrationRequest A data transfer object carrying the employee registration details.
     * @return APIResponse<AuthResponseDTO> Returns a new APIResponse object with the generated access token.
     * @throws EntityAlreadyExistsException if the provided employee email is already registered.
     * @throws ApplicationException         if any issues occur while accessing the database.
     */
    @Override
    public Employee registerEmployee(EmployeeRegistrationRequest registrationRequest) {
        LOGGER.debug("Attempting to register employee: {}", registrationRequest);
        String registrationEmail = registrationRequest.email();
        if (isEmailAlreadyRegistered(registrationEmail)) {
            throwEntityAlreadyExistsException(registrationEmail);
        }
        try {
            Employee employee = mapper.toEmployee(registrationRequest);
            return employeeRepository.save(employee);
        } catch (DataAccessException e) {
            LOGGER.error("An error occured trying to access the database", e);
            throw e;
        }
    }
    
    /**
     * Retrieves all stored employees
     *
     * @return List of EmployeeDTO for all employees.
     * @throws ApplicationException if any issues occur while accessing the database.
     */
    @Override
    public List<EmployeeDTO> getAllEmployees() {
        LOGGER.debug("Attempting to find all employees");
        try {
            List<EmployeeDTO> employeeDTOs = employeeRepository.findAll()
                    .stream()
                    .sorted(comparing(Employee::getLastname))
                    .map(mapper::toEmployeeDTO)
                    .toList();

            if (employeeDTOs.isEmpty()) {
                LOGGER.warn("EmployeeDTO list is empty.");
                throw new ApplicationException(HttpStatus.NOT_FOUND, "Employee list is empty");
            }
            LOGGER.info("EmployeeDTO contains {} employees", employeeDTOs.size());
            return employeeDTOs;
        } catch (DataAccessException e) {
            LOGGER.error("An error occured trying to access the database", e);
            throw e;
        }
    }

    /**
     * Retrieves an employee's details by the provided ID.
     *
     * @param id The ID of the employee to be retrieved.
     * @return the EmployeeDTO of the found employee.
     * @throws EntityNotFoundException if an employee with the provided ID is not found.
     * @throws ApplicationException    if any issues occur while accessing the database.
     */
    @Override
    public EmployeeDTO getEmployeeById(Long id) {
        LOGGER.debug("Attempting to find employee by ID: {}", id);
        try {
            return employeeRepository.findById(id)
                    .map(mapper::toEmployeeDTO)
                    .orElseThrow(() -> entityNotFoundException(id));
        } catch (DataAccessException e) {
            LOGGER.error("An error occured trying to access the database", e);
            throw e;
        }
    }

    @Override
    public EmployeeDTO getEmployeeByEmail(String email) {
        LOGGER.debug("Attempting to find employee: {}", email);
        try {
            return employeeRepository.findByEmail(email)
                    .map(mapper::toEmployeeDTO)
                    .orElseThrow(() -> {
                        LOGGER.error("Employee with email {} not found", email);
                        return new ApplicationException(HttpStatus.NOT_FOUND,
                                "Employee with email " + email + " not found");
                    });
        } catch (DataAccessException e) {
            LOGGER.error("An error occured trying to access the database", e);
            throw e;
        }
    }

    /**
     * Updates an existing employee's details using the provided EmployeeDTO.
     *
     * @param id          The ID of the employee to be updated.
     * @param employeeDto The new details for the employee.
     * @return EmployeeDTO indicating successful operation.
     * @throws EntityNotFoundException if an employee with the provided ID is not found.
     * @throws ApplicationException    if any issues occur while accessing the database.
     */
    @Override
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO employeeDto) {
        LOGGER.info("Attempting to update employee with ID: {}", id);
        try {
            return employeeRepository.findById(id)
                    .map(employee -> updateEmployeeWithDTOValues(employeeDto, employee))
                    .map(employeeRepository::save)
                    .map(mapper::toEmployeeDTO)
                    .orElseThrow(() -> entityNotFoundException(id));
        } catch (DataAccessException e) {
            LOGGER.error("An error occured trying to access the database", e);
            throw e;
        }
    }

    /**
     * Deletes an employee identified by the provided ID.
     *
     * @param id The ID of the employee to be deleted.
     * @throws EntityNotFoundException if an employee with the provided ID is not found.
     * @throws ApplicationException    if an issue occurs while accessing the database.
     */
    @Override
    public void deleteEmployee(Long id) {
        LOGGER.debug("Attempting to delete employee with ID: {}", id);
        try {
            Employee existingEmployee = employeeRepository.findById(id)
                    .orElseThrow(() -> entityNotFoundException(id));

            employeeRepository.deleteById(existingEmployee.getId());
        } catch (DataAccessException e) {
            LOGGER.error("An error occured trying to access the database", e);
            throw e;
        }
    }

    private Employee updateEmployeeWithDTOValues(EmployeeDTO employeeDTO, Employee employee) {
        return Employee.builder()
                .id(employee.getId())
                .firstname(getUpdatedStringValue.apply(employeeDTO.firstname(), employee.getFirstname()))
                .lastname(getUpdatedStringValue.apply(employeeDTO.lastname(), employee.getLastname()))
                .email(employee.getEmail())
                .password(employee.getPassword())
                .jobTitle(JobTitle.valueOf(getUpdatedStringValue.apply(employeeDTO.jobTitle(), employee.getJobTitle().name()).toUpperCase()))
                .role(Role.builder().roleType(getRoleType(employeeDTO, employee)).build())
                .build();
    }

    private static RoleType getRoleType(EmployeeDTO employeeDTO, Employee employee) {
        String roleTypeName = getUpdatedStringValue.apply(employeeDTO.role(), employee.getRole().getRoleType().getName());
        return RoleType.valueOf(roleTypeName.toUpperCase());
    }

    private static EntityNotFoundException entityNotFoundException(Long id) {
        String errorMessage = String.format("Could not find employee with ID: %d", id);
        EntityNotFoundException ex = new EntityNotFoundException(errorMessage, id);
        LOGGER.warn(errorMessage, ex);
        return ex;
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
        authenticationManager.authenticate(UsernamePasswordAuthenticationToken
                .unauthenticated(employeeLoginRequest.email(), employeeLoginRequest.password()));
    }
}
