package org.swyp.dessertbee.role.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.role.entity.RoleEntity;
import org.swyp.dessertbee.role.entity.RoleType;
import org.swyp.dessertbee.role.repository.RoleRepository;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleEntity findRoleByName(RoleType roleType) {
        return roleRepository.findByName(roleType)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleType));
    }
}