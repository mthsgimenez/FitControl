package com.mthsgimenez.fitcontrol.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "uuid", target = "userId")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "roles", target = "roles")
    UserResponseDTO toDTO(User user);

    default Set<RoleType> mapRoles(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(role -> {
                    String name = role.getNameUppercaseWithoutPrefix();
                    try {
                        return RoleType.valueOf(name);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
