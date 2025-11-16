package com.bank.auth.service;

import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.bank.auth.model.UserEntity;
import com.bank.auth.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        // create demo user if not exists
        if (userRepository.findByUsername("user").isEmpty()) {
            userRepository.save(new UserEntity("user", encoder.encode("password")));
        }
    }

    public boolean validateCredentials(String username, String rawPassword) {
        Optional<UserEntity> u = userRepository.findByUsername(username);
        if (u.isEmpty()) return false;
        return encoder.matches(rawPassword, u.get().getPassword());
    }

    public UserEntity register(String username, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("username exists");
        }
        UserEntity entity = new UserEntity(username, encoder.encode(password));
        return userRepository.save(entity);
    }
}
