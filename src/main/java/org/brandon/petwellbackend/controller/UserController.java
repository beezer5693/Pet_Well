package org.brandon.petwellbackend.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.brandon.petwellbackend.common.Mapper;
import org.brandon.petwellbackend.entity.UserEntity;
import org.brandon.petwellbackend.payload.*;
import org.brandon.petwellbackend.service.UserService;
import org.brandon.petwellbackend.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;
    private final Mapper mapper;

    @PostMapping("/auth/users/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Response<UserDTO> registerUser(@RequestBody @Valid UserRegistrationRequest request, HttpServletResponse response) {
        UserEntity registeredUserEntity = userService.registerUser(request);
        addJwtCookie(registeredUserEntity, response);
        return Response.success(mapper.toUserDTO(registeredUserEntity), HttpStatus.CREATED);
    }

    @GetMapping("/auth/users/{user-email}")
    public Response<Boolean> checkIfEmailAlreadyRegistered(@PathVariable("user-email") String userEmail) {
        return Response.success(userService.isEmailAlreadyRegistered(userEmail), HttpStatus.OK);
    }

    @GetMapping("/users")
    @PreAuthorize("hasAnyAuthority('admin:read')")
    public Response<List<UserDTO>> getAllUsers() {
        return Response.success(userService.getAllUsers(), HttpStatus.OK);
    }

    @GetMapping("/users/{user-id}")
    @PreAuthorize("hasAnyAuthority('admin:read')")
    public Response<UserDTO> getUserByID(@PathVariable("user-id") String userID) {
        return Response.success(userService.getUserByUserID(userID), HttpStatus.OK);
    }


    @PutMapping("/users/{user-id}")
    @PreAuthorize("hasAnyAuthority('admin:update')")
    public Response<UserDTO> updateUser(@PathVariable("user-id") String userID, @RequestBody @Valid UserDTO userDto) {
        return Response.success(userService.updateUser(userID, userDto), HttpStatus.OK);
    }

    @DeleteMapping("/users/{user-id}")
    @PreAuthorize("hasAnyAuthority('admin:delete')")
    public Response<?> deleteUser(@PathVariable("user-id") String userID) {
        userService.deleteUser(userID);
        return Response.success(null, HttpStatus.OK);
    }

    private void addJwtCookie(UserEntity userEntity, HttpServletResponse response) {
        jwtService.addCookie(response, userEntity);
    }
}
