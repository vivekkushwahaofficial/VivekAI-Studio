package com.vivekai.studio.user.service;

import com.vivekai.studio.exception.ResourceNotFoundException;
import com.vivekai.studio.exception.UserAlreadyExistsException;
import com.vivekai.studio.settings.entity.Settings;
import com.vivekai.studio.settings.repository.SettingsRepository;
import com.vivekai.studio.user.entity.Role;
import com.vivekai.studio.user.entity.User;
import com.vivekai.studio.user.entity.UserStatus;
import com.vivekai.studio.user.repository.RoleRepository;
import com.vivekai.studio.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SettingsRepository settingsRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(String username, String email, String rawPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException("Username is already taken");
        }
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email is already registered");
        }

        // Map ROLE_USER as default role
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Error: Role ROLE_USER is not found in database."));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .status(UserStatus.ACTIVE) // Auto-activating for simplicity until mail flow is added
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        // Pre-create standard default user settings
        Settings userSettings = Settings.builder()
                .user(savedUser)
                .theme("dark")
                .language("en")
                .fontSize(14)
                .responseStyle("balanced")
                .streamingEnabled(true)
                .build();
        settingsRepository.save(userSettings);

        return savedUser;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
