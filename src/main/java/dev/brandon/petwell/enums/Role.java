package dev.brandon.petwell.enums;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.brandon.petwell.enums.Permission.*;

public enum Role {

    CLIENT(Collections.emptySet()),
    ADMIN(Set.of(
            ADMIN_READ,
            ADMIN_CREATE,
            ADMIN_UPDATE,
            ADMIN_DELETE,
            MANAGER_READ,
            MANAGER_CREATE,
            MANAGER_UPDATE,
            MANAGER_DELETE
    )),
    MANAGER(Set.of(
            MANAGER_READ,
            MANAGER_CREATE,
            MANAGER_UPDATE,
            MANAGER_DELETE
    ));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public List<SimpleGrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = permissions.stream()
                .map(Permission::getPermission)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));

        return authorities;
    }
}
