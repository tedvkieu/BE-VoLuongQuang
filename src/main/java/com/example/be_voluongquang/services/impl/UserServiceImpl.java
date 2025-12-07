package com.example.be_voluongquang.services.impl;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.be_voluongquang.dto.request.UserRequestDTO;
import com.example.be_voluongquang.dto.request.UserRoleUpdateRequest;
import com.example.be_voluongquang.dto.response.UserResponseDTO;
import com.example.be_voluongquang.entity.UserEntity;
import com.example.be_voluongquang.exception.ResourceNotFoundException;
import com.example.be_voluongquang.exception.UserAlreadyExistsException;
import com.example.be_voluongquang.mapper.UserMapper;
import com.example.be_voluongquang.repository.UserRepository;
import com.example.be_voluongquang.services.UserService;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Set<String> ALLOWED_CREATE_ROLES = Set.of("CUSTOMER", "STAFF");

    @Override
    public Optional<UserResponseDTO> getUserById(String id) {
        return userRepository.findById(id).map(userMapper::toDto);
    }

    @Override
    @Transactional
    public UserResponseDTO createAUser(UserRequestDTO userRequest) {
        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new UserAlreadyExistsException(userRequest.getEmail());
        }

        String normalizedRole = userRequest.getRole() != null ? userRequest.getRole().trim().toUpperCase() : "CUSTOMER";
        if (!ALLOWED_CREATE_ROLES.contains(normalizedRole)) {
            throw new IllegalArgumentException("Role must be CUSTOMER hoặc STAFF");
        }

        String hashedPassword = passwordEncoder.encode(userRequest.getPassword());
        UserEntity user = UserEntity.builder()
                .fullName(userRequest.getFullName())
                .email(userRequest.getEmail())
                .password(hashedPassword)
                .phone(userRequest.getPhone())
                .address(userRequest.getAddress())
                .role(normalizedRole)
                .build();

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public java.util.List<UserResponseDTO> getUsers(String search, String role) {
        java.util.List<UserEntity> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userRepository.findByEmailOrFullNameContaining(search.trim());
        } else {
            users = userRepository.findAll();
        }

        if (role != null && !role.trim().isEmpty()) {
            String normalizedRole = role.trim().toUpperCase();
            users = users.stream()
                    .filter(u -> normalizedRole.equalsIgnoreCase(u.getRole()))
                    .toList();
        }

        return users.stream().map(userMapper::toDto).toList();
    }

    @Override
    @Transactional
    public UserResponseDTO updateUserRole(String id, UserRoleUpdateRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", id));

        String normalizedRole = request.getRole() != null ? request.getRole().trim().toUpperCase() : null;
        if (normalizedRole == null || normalizedRole.isEmpty()) {
            throw new IllegalArgumentException("Role must not be empty");
        }

        user.setRole(normalizedRole);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", "userId", id);
        }
        userRepository.deleteById(id);
    }
}
