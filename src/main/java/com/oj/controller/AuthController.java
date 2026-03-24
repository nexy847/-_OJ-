package com.oj.controller;

import java.time.Instant;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.oj.dto.CurrentUserResponse;
import com.oj.dto.LoginRequest;
import com.oj.dto.LoginResponse;
import com.oj.entity.User;
import com.oj.repository.UserRepository;
import com.oj.security.JwtTokenService;
import com.oj.service.RateLimitService;
import com.oj.util.HashUtils;
import com.oj.util.SecurityUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokenService;
    private final RateLimitService rateLimitService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenService tokenService,
                          RateLimitService rateLimitService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        rateLimitService.checkLogin(httpRequest.getRemoteAddr());
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        boolean ok = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());//有的用户密码用的是bcrypt
        if (!ok) {//有的用户密码加密用的是sha256
            String legacy = HashUtils.sha256(request.getPassword());
            if (legacy.equalsIgnoreCase(user.getPasswordHash())) {
                user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
                userRepository.save(user);
                ok = true;
            }
        }
        if (!ok) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        String token = tokenService.generateToken(user.getUsername());
        Instant expiresAt = Instant.now().plusSeconds(tokenService.getExpirationMinutes() * 60);
        return new LoginResponse(token, expiresAt);
    }

    @GetMapping("/me")
    public CurrentUserResponse me() {
        String username = SecurityUtils.currentUsername();
        if (username == null) {
            throw new AccessDeniedException("Forbidden");
        }
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String role = SecurityUtils.isAdmin() ? "ADMIN" : "USER";
        return new CurrentUserResponse(user.getId(), user.getUsername(), role);
    }
}
