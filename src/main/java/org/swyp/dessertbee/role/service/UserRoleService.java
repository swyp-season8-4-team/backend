package org.swyp.dessertbee.role.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.role.entity.RoleEntity;
import org.swyp.dessertbee.role.entity.UserRoleEntity;
import org.swyp.dessertbee.role.repository.UserRoleRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;

    // 유저의 역할 목록을 리스트로 가져옴
    public List<String> getUserRoles(UserEntity user) {
        return userRoleRepository.findByUserId(user.getId()).stream()
                .map(userRole -> userRole.getRole().getName().getRoleName())
                .collect(Collectors.toList());
    }
}
