package com.mthsgimenez.fitcontrol.user;

import com.mthsgimenez.fitcontrol.infra.exception.NotFoundWithIdentifierException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RoleService roleService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    public User createUser(CreateUserDTO data) {
        User newUser = new User();

        newUser.setEmail(data.email());
        String passwordHash = passwordEncoder.encode(data.password());
        newUser.setPasswordHash(passwordHash);
        newUser.setTenant(data.tenant());

        if (data.roles() != null && !data.roles().isEmpty()) {
            var roles = data.roles().stream().map(roleService::enumToEntity).collect(Collectors.toSet());
            newUser.setRoles(roles);
        }

        return userRepository.save(newUser);
    }

    public User addRoles(UUID userUUID, Set<RoleType> roles) {
        User user = userRepository.findByUuid(userUUID)
                .orElseThrow(() -> new NotFoundWithIdentifierException(User.class.getSimpleName(), userUUID));

        var newRoles = roles.stream().map(roleService::enumToEntity).collect(Collectors.toSet());

        Set<Role> currentRoles = user.getRoles();
        currentRoles.addAll(newRoles);

        return userRepository.save(user);
    }
}
