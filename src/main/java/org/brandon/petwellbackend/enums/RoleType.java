package org.brandon.petwellbackend.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.brandon.petwellbackend.enums.Permission.*;


@Getter
@RequiredArgsConstructor
public enum RoleType {
    ADMIN("Admin", Set.of(
            ADMIN_READ,
            ADMIN_CREATE,
            ADMIN_UPDATE,
            ADMIN_DELETE,
            MANAGER_READ,
            MANAGER_CREATE,
            MANAGER_UPDATE,
            MANAGER_DELETE
    )),
    MANAGER("manager", Set.of(
            MANAGER_READ,
            MANAGER_CREATE,
            MANAGER_UPDATE,
            MANAGER_DELETE
    )),
    CLIENT("client", Collections.emptySet());

    private final String name;
    private final Set<Permission> permissions;

    public List<SimpleGrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = permissions.stream()
                .map(Permission::getPermission)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}
