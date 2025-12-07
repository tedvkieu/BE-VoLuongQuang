package com.example.be_voluongquang.services;

import java.util.Optional;
import org.springframework.stereotype.Component;
import com.example.be_voluongquang.dto.request.UserRequestDTO;
import com.example.be_voluongquang.dto.request.UserRoleUpdateRequest;
import com.example.be_voluongquang.dto.response.UserResponseDTO;

@Component
public interface UserService {

    public Optional<UserResponseDTO> getUserById(String id);

    public UserResponseDTO createAUser(UserRequestDTO user);

    public java.util.List<UserResponseDTO> getUsers(String search, String role);

    public UserResponseDTO updateUserRole(String id, UserRoleUpdateRequest request);

    public void deleteUser(String id);

}
