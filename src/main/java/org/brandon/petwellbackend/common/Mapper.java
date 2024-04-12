package org.brandon.petwellbackend.common;

import lombok.RequiredArgsConstructor;
import org.brandon.petwellbackend.entity.Role;
import org.brandon.petwellbackend.entity.UserEntity;
import org.brandon.petwellbackend.enums.RoleType;
import org.brandon.petwellbackend.payload.UserDTO;
import org.brandon.petwellbackend.payload.UserRegistrationRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class Mapper {
    private final PasswordEncoder passwordEncoder;

    public UserEntity toUser(UserRegistrationRequest req) {
        Role userRole = Role.builder().roleType(RoleType.ADMIN).build();
        return UserEntity.builder()
                .userID(UUID.randomUUID().toString())
                .firstname(req.firstname())
                .lastname(req.lastname())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .role(userRole)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .isEnabled(true)
                .build();
    }

    public UserDTO toUserDTO(UserEntity userEntity) {
        return UserDTO.builder()
                .userID(userEntity.getUserID())
                .firstname(userEntity.getFirstname())
                .lastname(userEntity.getLastname())
                .email(userEntity.getEmail())
                .role(userEntity.getRole().getRoleType().getName())
                .isAccountNonExpired(userEntity.isAccountNonExpired())
                .isAccountNonLocked(userEntity.isAccountNonLocked())
                .isCredentialsNonExpired(userEntity.isCredentialsNonExpired())
                .isEnabled(userEntity.isEnabled())
                .build();
    }
}
