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
    private static final Set<String> ALLOWED_UPDATE_ROLES = Set.of("ADMIN", "CUSTOMER", "STAFF");

    @Override
    public Optional<UserResponseDTO> getUserById(String id) {
        return userRepository.findById(id)
                .filter(user -> !Boolean.TRUE.equals(user.getIsDeleted()))
                .map(userMapper::toDto);
    }

    @Override
    @Transactional
    public UserResponseDTO createAUser(UserRequestDTO userRequest) {
        // Kiểm tra email đã tồn tại chưa
        if (userRepository.existsByEmailIgnoreCaseAndIsDeletedFalse(userRequest.getEmail())) {
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
        user.setIsDeleted(false);

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public java.util.List<UserResponseDTO> getUsers(String search, String role, Boolean isDeleted) {
        Boolean deletedFilter = isDeleted != null ? isDeleted : Boolean.FALSE;
        java.util.List<UserEntity> users;
        if (search != null && !search.trim().isEmpty()) {
            users = userRepository.findByEmailOrFullNameContainingAndIsDeleted(search.trim(), deletedFilter);
        } else {
            users = userRepository.findByIsDeleted(deletedFilter);
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
    public UserResponseDTO updateUser(String id, UserRequestDTO request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", id));
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new ResourceNotFoundException("User", "userId", id);
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            String email = request.getEmail().trim();
            if (!email.equalsIgnoreCase(user.getEmail()) && userRepository.existsByEmailIgnoreCase(email)) {
                throw new UserAlreadyExistsException(email);
            }
            user.setEmail(email);
        }

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            String normalizedRole = request.getRole().trim().toUpperCase();
            if (!ALLOWED_UPDATE_ROLES.contains(normalizedRole)) {
                throw new IllegalArgumentException("Role must be CUSTOMER, STAFF hoặc ADMIN");
            }
            user.setRole(normalizedRole);
        }

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponseDTO updateUserRole(String id, UserRoleUpdateRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", id));
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new ResourceNotFoundException("User", "userId", id);
        }

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
        UserEntity target = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", id));

        if (!Boolean.TRUE.equals(target.getIsDeleted())) {
            target.setIsDeleted(true);
            userRepository.save(target);
        }
    }

    @Override
    @Transactional
    public UserResponseDTO restoreUser(String id) {
        UserEntity target = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", id));

        if (Boolean.FALSE.equals(target.getIsDeleted())) {
            return userMapper.toDto(target);
        }

        target.setIsDeleted(false);
        return userMapper.toDto(userRepository.save(target));
    }
}
