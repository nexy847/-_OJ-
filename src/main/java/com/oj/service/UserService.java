package com.oj.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.oj.entity.User;
import com.oj.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.oj.util.HashUtils;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User updateCurrentUser(User user, String username, String currentPassword, String newPassword) {
        boolean ok = passwordEncoder.matches(currentPassword, user.getPasswordHash());
        if (!ok) {
            String legacy = HashUtils.sha256(currentPassword);
            if (legacy.equalsIgnoreCase(user.getPasswordHash())) {
                ok = true;
            }
        }
        if (!ok) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        userRepository.findByUsername(username).ifPresent(existing -> {
            if (!existing.getId().equals(user.getId())) {
                throw new IllegalArgumentException("Username already exists");
            }
        });

        user.setUsername(username);
        if (newPassword != null && !newPassword.isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
        } else if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(currentPassword));
        }
        return userRepository.save(user);
    }
}
