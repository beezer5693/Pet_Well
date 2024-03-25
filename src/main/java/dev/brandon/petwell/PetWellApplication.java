package dev.brandon.petwell;

import dev.brandon.petwell.auth.AuthenticationService;
import dev.brandon.petwell.auth.RegisterRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PetWellApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetWellApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(AuthenticationService authenticationService) {
        return args -> {
            RegisterRequest r1 = new RegisterRequest(
                    "Brandon",
                    "Bryan",
                    "brandon@petwell.com",
                    "password",
                    "veterinarian",
                    "admin");

            System.out.println("Admin Token: " + authenticationService.register(r1).getData().token().getToken());

            RegisterRequest r2 = new RegisterRequest(
                    "Arantxa",
                    "Leon",
                    "arantxa@petwell.com",
                    "password",
                    "veterinarian",
                    "manager");

            System.out.println("Manager Token: " + authenticationService.register(r2).getData().token().getToken());
        };
    }
}
