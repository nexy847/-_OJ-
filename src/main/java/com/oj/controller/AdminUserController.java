package com.oj.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.oj.dto.CreateUserRequest;
import com.oj.dto.UserResponse;
import com.oj.entity.User;
import com.oj.repository.UserRepository;
import com.oj.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final UserService userService;
    private final UserRepository userRepository;

    public AdminUserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        userRepository.findByUsername(request.getUsername()).ifPresent(existing -> {
            throw new IllegalArgumentException("Username already exists");
        });
        User user = userService.createUser(request.getUsername(), request.getPassword());
        return new UserResponse(user.getId(), user.getUsername(), user.getCreatedAt());
    }
}
