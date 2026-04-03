package com.FDPACB.FDPACB.service;

import com.FDPACB.FDPACB.dto.auth.AuthResponse;
import com.FDPACB.FDPACB.dto.auth.LoginRequest;
import com.FDPACB.FDPACB.dto.auth.RegisterRequest;
import com.FDPACB.FDPACB.entity.User;
import com.FDPACB.FDPACB.exception.ConflictException;
import com.FDPACB.FDPACB.repository.UserRepository;
import com.FDPACB.FDPACB.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new ConflictException("Username '" + req.username() + "' is already taken");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new ConflictException("Email '" + req.email() + "' is already registered");
        }

        User user = new User(
                req.username(),
                req.email(),
                passwordEncoder.encode(req.password()),
                req.role()
        );
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return AuthResponse.of(token, user.getUsername(), user.getRole().name(), expirationMs);
    }

    public AuthResponse login(LoginRequest req) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );

        User user = userRepository.findByUsername(req.username()).orElseThrow();
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return AuthResponse.of(token, user.getUsername(), user.getRole().name(), expirationMs);
    }
}
