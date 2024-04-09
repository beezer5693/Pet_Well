package org.brandon.petwellbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.brandon.petwellbackend.domain.Auditable;
import org.brandon.petwellbackend.enums.RoleType;

@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Table(name = "roles")
public class Role extends Auditable {
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Role cannot be null")
    private RoleType roleType;
}
