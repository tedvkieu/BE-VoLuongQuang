package com.example.be_voluongquang.services;

import java.util.Optional;
import org.springframework.stereotype.Component;
import com.example.be_voluongquang.dto.request.UserRequestDTO;
import com.example.be_voluongquang.dto.response.UserResponseDTO;

@Component
public interface UserService {

    public Optional<UserResponseDTO> getUserById(String id);

    public UserResponseDTO createAUser(UserRequestDTO user);

    public UserResponseDTO updateAUser(String id, UserRequestDTO userRequestDTO);

    public void deleteAUser(String id);

}
