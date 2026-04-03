package com.FDPACB.FDPACB.service;

import com.FDPACB.FDPACB.dto.user.UpdateUserRequest;
import com.FDPACB.FDPACB.dto.user.UserResponse;
import com.FDPACB.FDPACB.entity.User;
import com.FDPACB.FDPACB.exception.ResourceNotFoundException;
import com.FDPACB.FDPACB.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (req.role() != null) {
            user.setRole(req.role());
        }
        if (req.active() != null) {
            user.setActive(req.active());
        }

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public void deactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setActive(false);
        userRepository.save(user);
    }
}
