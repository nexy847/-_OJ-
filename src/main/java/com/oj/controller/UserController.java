package com.oj.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.oj.dto.CreateUserRequest;
import com.oj.dto.UpdateCurrentUserRequest;
import com.oj.dto.UserResponse;
import com.oj.entity.User;
import com.oj.repository.UserRepository;
import com.oj.service.UserService;
import com.oj.util.SecurityUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return createUserResponse(request);
    }

    private UserResponse createUserResponse(CreateUserRequest request) {
        userRepository.findByUsername(request.getUsername()).ifPresent(existing -> {
            throw new IllegalArgumentException("Username already exists");
        });
        User user = userService.createUser(request.getUsername(), request.getPassword());
        return new UserResponse(user.getId(), user.getUsername(), user.getCreatedAt());
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable("id") Long id) {
        User user = userService.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!SecurityUtils.isAdmin()) {
            String username = SecurityUtils.currentUsername();
            if (username == null || !username.equalsIgnoreCase(user.getUsername())) {
                throw new AccessDeniedException("Forbidden");
            }
        }
        return new UserResponse(user.getId(), user.getUsername(), user.getCreatedAt());
    }

    @PutMapping("/me")
    public UserResponse updateCurrentUser(@Valid @RequestBody UpdateCurrentUserRequest request) {
        String username = SecurityUtils.currentUsername();
        if (username == null) {
            throw new AccessDeniedException("Forbidden");
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AccessDeniedException("Forbidden"));
        User updated = userService.updateCurrentUser(user, request.getUsername(), request.getCurrentPassword(), request.getNewPassword());
        return new UserResponse(updated.getId(), updated.getUsername(), updated.getCreatedAt());
    }
}
