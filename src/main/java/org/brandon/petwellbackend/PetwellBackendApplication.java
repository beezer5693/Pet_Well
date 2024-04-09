package org.brandon.petwellbackend;

import org.brandon.petwellbackend.entity.Role;
import org.brandon.petwellbackend.enums.RoleType;
import org.brandon.petwellbackend.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class PetwellBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetwellBackendApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(RoleRepository roleRepository) {
        return args -> {
            Role adminRole = Role.builder()
                    .roleType(RoleType.ADMIN)
                    .build();

            Role managerRole = Role.builder()
                    .roleType(RoleType.MANAGER)
                    .build();

            roleRepository.saveAll(List.of(adminRole, managerRole));
        };
    }

}
