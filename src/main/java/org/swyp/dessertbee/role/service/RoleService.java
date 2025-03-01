package org.swyp.dessertbee.role.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.role.entity.RoleEntity;
import org.swyp.dessertbee.role.entity.RoleType;
import org.swyp.dessertbee.role.repository.RoleRepository;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    /**
     * 역할 유형으로 역할 엔티티를 조회
     *
     * @param roleType 조회할 역할 유형
     * @return 역할 엔티티
     * @throws BusinessException 역할을 찾을 수 없는 경우
     */
    public RoleEntity findRoleByType(RoleType roleType) {
        return roleRepository.findByName(roleType)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_INPUT_VALUE,
                        "요청한 사용자 역할을 찾을 수 없습니다: " + roleType));
    }
}