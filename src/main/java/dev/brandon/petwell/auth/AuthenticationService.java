package dev.brandon.petwell.auth;

import dev.brandon.petwell.employee.Employee;
import dev.brandon.petwell.employee.EmployeeMapper;
import dev.brandon.petwell.employee.EmployeeRepository;
import dev.brandon.petwell.exceptions.ApplicationException;
import dev.brandon.petwell.jwt.JwtService;
import dev.brandon.petwell.responses.ApiResponse;
import dev.brandon.petwell.security.EmployeeDetails;
import dev.brandon.petwell.token.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import static dev.brandon.petwell.token.TokenType.BEARER;

@Service
public class AuthenticationService {

    private final static Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            EmployeeRepository employeeRepository,
            EmployeeMapper employeeMapper,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public ApiResponse<AuthenticationResponse> register(RegisterRequest request) {
        boolean isEmployeeEmailExisting = employeeRepository.existsByEmail(request.email());

        if (isEmployeeEmailExisting) {
            LOGGER.error("Employee with email {} already exists", request.email());
            throw new ApplicationException(HttpStatus.CONFLICT, "Employee Already Exists", null);
        }

        Employee savedEmployee = employeeRepository.save(employeeMapper.mapToEmployee(request));

        Token token = new Token(jwtService.generateToken(new EmployeeDetails(savedEmployee)), BEARER, false, false);

        AuthenticationResponse authResponse = new AuthenticationResponse(token);

        return ApiResponse.successfulResponse(HttpStatus.CREATED.value(), "Successful", authResponse);
    }

    public ApiResponse<AuthenticationResponse> authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        AuthenticationResponse authResponse = employeeRepository.findByEmail(request.email())
                .map(employee -> new Token(jwtService.generateToken(new EmployeeDetails(employee)), BEARER, false, false))
                .map(AuthenticationResponse::new)
                .orElseThrow(() -> {
                    LOGGER.error("User with email {} could not be found", request.email());
                    return new ApplicationException(HttpStatus.NOT_FOUND, "User not found");
                });


        return ApiResponse.successfulResponse(HttpStatus.OK.value(), "Successful", authResponse);
    }
}
