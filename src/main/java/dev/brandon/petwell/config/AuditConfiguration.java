package dev.brandon.petwell.config;

import dev.brandon.petwell.audit.ApplicationAuditAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class AuditConfiguration {

    @Bean
    AuditorAware<String> auditorAware() {
        return new ApplicationAuditAware();
    }
}
