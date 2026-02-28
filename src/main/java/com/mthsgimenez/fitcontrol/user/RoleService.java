package com.mthsgimenez.fitcontrol.user;

import org.springframework.stereotype.Service;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final String rolePrefix = "ROLE_";

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role enumToEntity(RoleType role) {
        return roleRepository.findByName(rolePrefix + role)
                .orElseThrow(() -> new IllegalStateException(rolePrefix + role + " not found in database"));
    }

    public RoleType entityToEnum(Role role) {
        String name = role.getNameUppercaseWithoutPrefix();
        try {
            return RoleType.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("RoleType not found for role: " + role.getName());
        }
    }

//    public Set<Role> enumToEntity(Set<RoleType> roles) {
//        return roles.stream().map(
//                roleName -> roleRepository.findByName(rolePrefix + roleName)
//                        .orElseThrow(() -> new IllegalStateException(rolePrefix + roleName + " not found in database"))
//        ).collect(Collectors.toSet());
//    }
//
//    public Set<RoleType> entityToEnum(Set<Role> roles) {
//        return roles.stream()
//                .map( role -> {
//                    String name = role.getName().substring(5).toUpperCase();
//                    return RoleType.valueOf(name);
//                }).collect(Collectors.toSet());
//    }
}
