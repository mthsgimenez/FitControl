package com.mthsgimenez.fitcontrol.auth.service;

import com.mthsgimenez.fitcontrol.auth.dto.UserRegisterDTO;
import com.mthsgimenez.fitcontrol.auth.model.User;
import com.mthsgimenez.fitcontrol.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserRegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserRegisterService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerNewUser(UserRegisterDTO data) {
        User newUser = new User();

        newUser.setEmail(data.email());
        String passwordHash = passwordEncoder.encode(data.password());
        newUser.setPasswordHash(passwordHash);
        newUser.setTenant(data.tenant());

        if (data.roles() != null && !data.roles().isEmpty()) {
            newUser.setRoles(data.roles());
        }

        return userRepository.save(newUser);
    }
}
