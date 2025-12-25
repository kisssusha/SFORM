package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.UserRequest;
import org.example.dto.response.UserResponse;
import org.example.entity.User;
import org.example.exception.InvalidRequestException;
import org.example.mapper.UserMapper;
import org.example.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;


    @PostMapping
    public UserResponse createUser(@RequestBody UserRequest userRequest) {
        if (userRequest == null) {
            throw new InvalidRequestException("User request is null");
        }
        User entity = userMapper.toEntity(userRequest);
        User user = userService.createUser(entity);

        return userMapper.toResponse(user);
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService
                .getAll()
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @RequestBody UserRequest userRequest
    ) {
        if (id == null) {
            throw new InvalidRequestException("User id is null");
        }
        User updated = userService.updateUser(id, userRequest);

        return userMapper.toResponse(updated);
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("User id is null");
        }
        User user = userService.getUserById(id);
        return userMapper.toResponse(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (id == null) {
            throw new InvalidRequestException("User id is null");
        }
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
