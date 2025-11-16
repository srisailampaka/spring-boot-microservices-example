package com.bank.auth.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.bank.auth.repository.UserRepository;
import com.bank.auth.model.UserEntity;

/**
 * Admin endpoints for inspecting users. Protected by JWT in SecurityConfig.
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public List<?> listUsers() {
        List<UserEntity> users = userRepository.findAll();
        return users.stream().map(u -> java.util.Map.of("id", u.getId(), "username", u.getUsername())).collect(Collectors.toList());
    }
}
