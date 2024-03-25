package dev.brandon.petwell.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.brandon.petwell.employee.EmployeeRepository;
import dev.brandon.petwell.exceptions.ApplicationException;
import dev.brandon.petwell.security.EmployeeDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ApplicationConfiguration {

    private final EmployeeRepository employeeRepository;

    public ApplicationConfiguration(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Bean
    UserDetailsService userDetailsService() {
        return username -> employeeRepository.findByEmail(username)
                .map(EmployeeDetails::new)
                .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
