package org.brandon.petwellbackend.service;

import org.brandon.petwellbackend.entity.UserEntity;
import org.brandon.petwellbackend.payload.UserDTO;
import org.brandon.petwellbackend.payload.UserRegistrationRequest;

import java.util.List;

public interface UserService {

    UserEntity registerUser(UserRegistrationRequest registrationRequest);

    List<UserDTO> getAllUsers();

    UserDTO getUserByUserID(String userID);

    UserEntity getUserByEmail(String email);

    UserDTO updateUser(String userID, UserDTO userDto);

    void deleteUser(String userID);

    boolean isEmailAlreadyRegistered(String email);
}
