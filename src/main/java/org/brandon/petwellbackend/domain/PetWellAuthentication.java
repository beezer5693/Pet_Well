package org.brandon.petwellbackend.domain;

import lombok.Getter;
import org.brandon.petwellbackend.entity.Employee;
import org.brandon.petwellbackend.exception.ApplicationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Collection;

@Getter
public class PetWellAuthentication extends AbstractAuthenticationToken {
    private static final String PASSWORD_PROTECTED = "[PASSWORD_PROTECTED]";
    private static final String EMAIL_PROTECTED = "[EMAIL_PROTECTED]";

    private Employee employee;
    private final String email;
    private final String password;
    private final boolean isAuthenticated;

    @Override
    public void setAuthenticated(boolean authenticated) {
        throw new ApplicationException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot set authentication");
    }

    @Override
    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    private PetWellAuthentication(String email, String password) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.password = password;
        this.email = email;
        this.isAuthenticated = false;
    }

    private PetWellAuthentication(Employee employee, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.employee = employee;
        this.password = PASSWORD_PROTECTED;
        this.email = EMAIL_PROTECTED;
        this.isAuthenticated = true;
    }

    public static PetWellAuthentication unAuthenticated(String email, String password) {
        return new PetWellAuthentication(email, password);
    }

    public static PetWellAuthentication authenticated(Employee employee, Collection<? extends GrantedAuthority> authorities) {
        return new PetWellAuthentication(employee, authorities);
    }

    @Override
    public Object getCredentials() {
        return PASSWORD_PROTECTED;
    }

    @Override
    public Object getPrincipal() {
        return employee;
    }
}
