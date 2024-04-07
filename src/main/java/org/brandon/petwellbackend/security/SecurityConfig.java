package org.brandon.petwellbackend.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AuthenticationProvider employeeAuthProvider;
    private final AuthenticationProvider clientAuthProvider;
    private final CustomLogoutHandler customLogoutHandler;
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(
            @Qualifier("employee_auth_provider")
            AuthenticationProvider employeeAuthProvider,
            @Qualifier("client_auth_provider")
            AuthenticationProvider clientAuthProvider,
            CustomLogoutHandler customLogoutHandler,
            JwtAuthFilter jwtAuthFilter
    ) {
        this.employeeAuthProvider = employeeAuthProvider;
        this.clientAuthProvider = clientAuthProvider;
        this.customLogoutHandler = customLogoutHandler;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    SecurityFilterChain authSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .securityMatcher(new AntPathRequestMatcher("/api/v1/auth/**"));

        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/**")
                        .permitAll()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS));

        http.logout(logout -> logout
                .logoutUrl("/api/v1/auth/logout")
                .addLogoutHandler(customLogoutHandler)
                .logoutSuccessHandler(((_, _, _) -> SecurityContextHolder.clearContext())));

        return http.build();
    }

    @Bean
    SecurityFilterChain employeeSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .securityMatcher(new AntPathRequestMatcher("/api/v1/employees/**"));

        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/employees/**")
                        .authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(employeeAuthProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    SecurityFilterChain clientSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .securityMatcher(new AntPathRequestMatcher("/api/v1/client/**"));

        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/employees/**")
                        .authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(clientAuthProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .securityMatcher(new AntPathRequestMatcher("/actuator/**"));

        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**")
                        .permitAll()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS));

        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
