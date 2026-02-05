package com.example.be_voluongquang.services;

import java.util.Optional;
import org.springframework.stereotype.Component;
import com.example.be_voluongquang.dto.request.UserRequestDTO;
import com.example.be_voluongquang.dto.request.UserRoleUpdateRequest;
import com.example.be_voluongquang.dto.request.UserProfileUpdateRequest;
import com.example.be_voluongquang.dto.response.UserProfileDTO;
import com.example.be_voluongquang.dto.response.UserResponseDTO;
import org.springframework.web.multipart.MultipartFile;

@Component
public interface UserService {

    public Optional<UserResponseDTO> getUserById(String id);

    public UserResponseDTO createAUser(UserRequestDTO user);

    public java.util.List<UserResponseDTO> getUsers(String search, String role, Boolean isDeleted);

    public UserResponseDTO updateUser(String id, UserRequestDTO user);

    public UserResponseDTO updateUserRole(String id, UserRoleUpdateRequest request);

    public void deleteUser(String id);

    public UserResponseDTO restoreUser(String id);

    public UserProfileDTO getUserProfile(String userId);

    public UserProfileDTO updateProfile(String userId, UserProfileUpdateRequest request);

    public UserProfileDTO updateAvatar(String userId, MultipartFile avatar);

}
