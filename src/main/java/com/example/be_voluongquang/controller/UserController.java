package com.example.be_voluongquang.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.be_voluongquang.dto.request.UserRequestDTO;
import com.example.be_voluongquang.dto.request.UserRoleUpdateRequest;
import com.example.be_voluongquang.dto.response.UserResponseDTO;
import com.example.be_voluongquang.services.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/api/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping()
    public List<UserResponseDTO> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role) {
        return userService.getUsers(search, role);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @PostMapping()
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody UserRequestDTO entity) {

        UserResponseDTO user = userService.createAUser(entity);

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponseDTO> updateRole(
            @PathVariable String id,
            @Valid @RequestBody UserRoleUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUserRole(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}
