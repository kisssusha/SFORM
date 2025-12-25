package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.UserRequest;
import org.example.entity.User;
import org.example.exception.EntityNotFoundException;
import org.example.exception.ExistEntityException;
import org.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public List<User> getAll() {
        List<User> users = userRepository.findAll();
        log.debug("Fetched {} user(s)", users.size());
        return users;
    }

    public User getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("User not found: ID=%d", id)
                ));
        log.debug("Fetched User: ID={}, Name='{}', Email='{}', Role={}",
                id, user.getName(), user.getEmail(), user.getRole());
        return user;
    }

    public User createUser(User user) {
        try {
            User saved = userRepository.save(user);
            log.info("Created User: ID={}, Name='{}', Email='{}', Role={}",
                    saved.getId(), saved.getName(), saved.getEmail(), saved.getRole());
            return saved;
        } catch (DataIntegrityViolationException ex) {
            log.warn("Attempt to create user with duplicate email: '{}'", user.getEmail());
            throw new ExistEntityException(
                    String.format("User with email '%s' already exists.", user.getEmail())
            );
        }
    }

    public User updateUser(Long id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("User not found: ID=%d", id)
                ));

        boolean updated = false;

        if (request.getName() != null && !request.getName().equals(user.getName())) {
            user.setName(request.getName());
            log.debug("Updated name for User ID={}: '{}'", id, request.getName());
            updated = true;
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            user.setEmail(request.getEmail());
            log.debug("Updated email for User ID={}: '{}'", id, request.getEmail());
            updated = true;
        }

        if (request.getRole() != null) {
            try {
                User.Role role = User.Role.valueOf(request.getRole().toUpperCase());
                if (!role.equals(user.getRole())) {
                    user.setRole(role);
                    log.debug("Updated role for User ID={}: {}", id, role);
                    updated = true;
                }
            } catch (IllegalArgumentException e) {
                log.warn("Invalid role value provided: '{}' for User ID={}", request.getRole(), id);
                throw new IllegalArgumentException("Invalid role: " + request.getRole());
            }
        }

        if (!updated) {
            log.debug("No changes detected for User: ID={}", id);
            return user;
        }

        User saved = userRepository.save(user);
        log.info("Successfully updated User: ID={}, Name='{}', Email='{}', Role={}",
                id, saved.getName(), saved.getEmail(), saved.getRole());
        return saved;
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("User not found: ID=%d", id)
                ));

        userRepository.delete(user);

        log.info("Deleted User: ID={}, Name='{}', Email='{}', Role={}",
                id, user.getName(), user.getEmail(), user.getRole());
    }
}
