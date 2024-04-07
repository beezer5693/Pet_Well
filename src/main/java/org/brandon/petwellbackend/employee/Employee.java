package org.brandon.petwellbackend.employee;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.brandon.petwellbackend.common.User;

@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "employees")
public class Employee extends User {

    @Column(name = "job_title", nullable = false)
    @NotNull(message = "Job title is required")
    @Enumerated(EnumType.STRING)
    private JobTitle jobTitle;
}
