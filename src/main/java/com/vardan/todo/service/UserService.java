package com.vardan.todo.service;

import com.vardan.todo.dto.request.RegisterRequest;
import com.vardan.todo.entity.User;
import com.vardan.todo.enums.AuthProvider;
import com.vardan.todo.enums.Role;
import com.vardan.todo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;


    public User createUser(RegisterRequest request)
    {
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Scrambling!
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.USER) // Default role
                .enabled(true)
                .provider(AuthProvider.LOCAL)
                .build();
        userRepository.save(user);
        return user;
    }

}
