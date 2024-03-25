package dev.brandon.petwell.auth;

import dev.brandon.petwell.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> register(@RequestBody @Valid RegisterRequest request, Authentication authentication) {
        ApiResponse<AuthenticationResponse> response = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<ApiResponse> authenticate(@RequestBody AuthenticationRequest request) {
        ApiResponse<AuthenticationResponse> response = authenticationService.authenticate(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
