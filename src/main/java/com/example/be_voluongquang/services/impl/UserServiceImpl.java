package com.example.be_voluongquang.services.impl;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.be_voluongquang.dto.request.UserRequestDTO;
import com.example.be_voluongquang.dto.response.UserResponseDTO;
import com.example.be_voluongquang.entity.UserEntity;
import com.example.be_voluongquang.exception.ResourceNotFoundException;
import com.example.be_voluongquang.exception.UserAlreadyExistsException;
import com.example.be_voluongquang.exception.UserNotFoundException;
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

        String hashedPassword = passwordEncoder.encode(userRequest.getPassword());
        UserEntity user = UserEntity.builder()
                .fullName(userRequest.getFullName())
                .email(userRequest.getEmail())
                .password(hashedPassword)
                .phone(userRequest.getPhone())
                .address(userRequest.getAddress())
                .role("CUSTOMER")
                .build();

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserResponseDTO updateAUser(String id, UserRequestDTO userRequestDTO) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        // Cập nhật thông tin người dùng
        user.setFullName(userRequestDTO.getFullName());
        user.setEmail(userRequestDTO.getEmail());
        user.setPhone(userRequestDTO.getPhone());
        user.setAddress(userRequestDTO.getAddress());

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteAUser(String id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        userRepository.delete(user);
    }
}