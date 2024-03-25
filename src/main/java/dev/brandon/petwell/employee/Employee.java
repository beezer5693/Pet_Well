package dev.brandon.petwell.employee;

import dev.brandon.petwell.audit.Auditable;
import dev.brandon.petwell.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "employees")
public class Employee extends Auditable implements Serializable {

    @Id
    @SequenceGenerator(name = "employees_generator", sequenceName = "employees_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "employee_id")
    private Long id;

    @Column(name = "first_name", nullable = false, length = 50)

    @NotNull(message = "First name cannot be null")
    @NotEmpty(message = "First name cannot be empty")
    @NotBlank(message = "First name cannot be blank")
    @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z]*$", message = "First name must contain only letters")
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    @NotNull(message = "Last name cannot be null")
    @NotEmpty(message = "Last name cannot be empty")
    @NotBlank(message = "Last name cannot be blank")
    @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z]*$", message = "Last name must contain only letters")
    private String lastName;

    @Column(unique = true, nullable = false)
    @NotNull(message = "Email cannot be null")
    @NotEmpty(message = "Email cannot be blank")
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email address")
    private String email;

    @Column(nullable = false)
    @NotNull(message = "Password cannot be null")
    @NotEmpty(message = "Password cannot be empty")
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @Column(name = "job_title", nullable = false)
    @NotNull(message = "Job title cannot be null")
    @Enumerated(EnumType.STRING)
    private JobTitle jobTitle;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Role cannot be null")
    private Role role;

    public Employee() {
    }

    public Employee(String firstName, String lastName, String email, String password, JobTitle jobTitle, Role role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.jobTitle = jobTitle;
        this.role = role;
    }

    public Employee(Long id, String firstName, String lastName, String email, String password, JobTitle jobTitle, Role role) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.jobTitle = jobTitle;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public JobTitle getJobTitle() {
        return jobTitle;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", jobTitle=" + jobTitle +
                ", role=" + role +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(id, employee.id)
                && Objects.equals(firstName, employee.firstName)
                && Objects.equals(lastName, employee.lastName)
                && Objects.equals(email, employee.email)
                && Objects.equals(password, employee.password)
                && jobTitle == employee.jobTitle
                && role == employee.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, email, password, jobTitle, role);
    }
}
