package com.example.be_voluongquang.mapper;

import org.springframework.stereotype.Component;

import com.example.be_voluongquang.dto.response.UserResponseDTO;
import com.example.be_voluongquang.entity.UserEntity;

@Component
public class UserMapper extends BaseMapper<UserEntity, UserResponseDTO> {
    public UserMapper() {
        super(UserEntity.class, UserResponseDTO.class);
    }
}
