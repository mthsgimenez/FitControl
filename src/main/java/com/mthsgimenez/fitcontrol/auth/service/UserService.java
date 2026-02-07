package com.mthsgimenez.fitcontrol.auth.service;

import com.mthsgimenez.fitcontrol.auth.dto.CreateUserDTO;
import com.mthsgimenez.fitcontrol.auth.model.User;
import com.mthsgimenez.fitcontrol.auth.repository.RoleRepository;
import com.mthsgimenez.fitcontrol.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final String rolePrefix = "ROLE_";

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RoleRepository roleRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public User createUser(CreateUserDTO data) {
        User newUser = new User();

        newUser.setEmail(data.email());
        String passwordHash = passwordEncoder.encode(data.password());
        newUser.setPasswordHash(passwordHash);
        newUser.setTenant(data.tenant());

        if (data.roles() != null && !data.roles().isEmpty()) {
            var roles = data.roles().stream().map(
                    roleName -> roleRepository.findByName(rolePrefix + roleName)
                            .orElseThrow(() -> new IllegalStateException(rolePrefix + roleName + " not found in database"))
            ).collect(Collectors.toSet());
            newUser.setRoles(roles);
        }

        return userRepository.save(newUser);
    }
}
