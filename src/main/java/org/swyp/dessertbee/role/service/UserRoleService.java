package org.swyp.dessertbee.role.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.role.entity.RoleEntity;
import org.swyp.dessertbee.role.entity.RoleType;
import org.swyp.dessertbee.role.repository.RoleRepository;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 역할 관리를 위한 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserRoleService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    /**
     * 사용자의 역할 목록을 조회
     *
     * @param user 역할을 조회할 사용자
     * @return 사용자의 역할 목록 (String 형태의 역할명)
     */
    public List<String> getUserRoles(UserEntity user) {
        if (user.getUserRoles() == null || user.getUserRoles().isEmpty()) {
            return Collections.emptyList();
        }

        return user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getName().getRoleName())
                .collect(Collectors.toList());
    }

    /**
     * 사용자에게 역할이 있는지 확인
     *
     * @param user 확인할 사용자
     * @param roleType 확인할 역할 유형
     * @return 해당 역할이 있으면 true, 없으면 false
     */
    public boolean hasUserRole(UserEntity user, RoleType roleType) {
        if (user.getUserRoles() == null || user.getUserRoles().isEmpty()) {
            return false;
        }

        return user.getUserRoles().stream()
                .anyMatch(userRole -> userRole.getRole().getName() == roleType);
    }

    /**
     * 사용자에게 역할이 없는 경우 기본 역할을 설정
     *
     * @param user 역할을 설정할 사용자
     * @return 설정된 역할 목록
     */
    @Transactional
    public List<String> ensureDefaultRole(UserEntity user) {
        if (user.getUserRoles() == null || user.getUserRoles().isEmpty()) {
            return addUserRole(user, RoleType.ROLE_USER);
        }
        return getUserRoles(user);
    }

    /**
     * 사용자에게 역할을 추가하고 이미 가지고 있는 역할은 중복 추가하지 않는다.
     *
     * @param user 역할을 추가할 사용자
     * @param roleType 추가할 역할
     * @return 업데이트된 역할 목록
     */
    @Transactional
    public List<String> addUserRole(UserEntity user, RoleType roleType) {
        if (!hasUserRole(user, roleType)) {
            RoleEntity role = roleService.findRoleByType(roleType);
            user.addRole(role);
            userRepository.save(user);

            log.info("사용자에게 역할 추가 - 이메일: {}, 역할: {}",
                    user.getEmail(), roleType.getRoleName());
        }

        return getUserRoles(user);
    }

    /**
     * 사용자의 모든 역할을 제거
     *
     * @param user 역할을 제거할 사용자
     */
    @Transactional
    public void clearUserRoles(UserEntity user) {
        if (user.getUserRoles() != null) {
            user.getUserRoles().clear();
            userRepository.save(user);
            log.info("사용자의 모든 역할 제거 완료 - 이메일: {}", user.getEmail());
        }
    }

    /**
     * 사용자의 모든 역할을 제거하고 새 역할 목록을 설정
     *
     * @param user 역할을 설정할 사용자
     * @param roleTypes 설정할 역할 목록
     * @return 설정된 역할 목록
     */
    @Transactional
    public List<String> setUserRoles(UserEntity user, List<RoleType> roleTypes) {
        if (roleTypes == null || roleTypes.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "최소 하나 이상의 역할이 필요합니다.");
        }

        // 기존 역할 관계 모두 제거
        clearUserRoles(user);

        // 새 역할 설정
        List<String> assignedRoles = new ArrayList<>();
        for (RoleType roleType : roleTypes) {
            RoleEntity role = roleService.findRoleByType(roleType);
            user.addRole(role);
            assignedRoles.add(roleType.getRoleName());
        }

        userRepository.save(user);
        log.info("사용자 역할 다중 설정 완료 - 이메일: {}, 역할: {}", user.getEmail(), assignedRoles);

        return assignedRoles;
    }

    /**
     * 사용자에게서 특정 역할을 제거
     *
     * @param user 역할을 제거할 사용자
     * @param roleType 제거할 역할 유형
     * @return 역할 제거 후 남은 역할 목록
     */
    @Transactional
    public List<String> removeUserRole(UserEntity user, RoleType roleType) {
        if (user.getUserRoles() != null) {
            // 제거할 UserRole 엔티티 찾기
            user.getUserRoles().removeIf(userRole ->
                    userRole.getRole().getName() == roleType);

            userRepository.save(user);
            log.info("사용자에게서 역할 제거 - 이메일: {}, 역할: {}",
                    user.getEmail(), roleType.getRoleName());
        }

        return getUserRoles(user);
    }

    /**
     * 사용자의 역할(권한) 정보를 업데이트합니다.
     * ROLE_ADMIN 권한은 부여할 수 없습니다.
     */
    @Transactional
    public void updateUserRoles(UserEntity user, List<String> roleNames) {
        // 역할이 null이거나 비어있으면 업데이트하지 않음
        if (roleNames == null || roleNames.isEmpty()) {
            return;
        }

        // ADMIN 권한 부여 시도 여부 확인
        boolean attemptToGrantAdmin = roleNames.stream()
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (attemptToGrantAdmin) {
            throw new BusinessException(ErrorCode.FORBIDDEN_OPERATION,
                    "ADMIN 권한은 부여할 수 없습니다.");
        }

        // 사장 권한 부여 시도 여부 확인
        boolean attemptToGrantOwner = roleNames.stream()
                .anyMatch(role -> role.equals("ROLE_OWNER"));

        // 사장 권한 부여 시 이름과 전화번호 유효성 검증
        if (attemptToGrantOwner) {
            if (user.getName() == null || user.getName().trim().isEmpty() ||
                    user.getPhoneNumber() == null || user.getPhoneNumber().trim().isEmpty()) {
                throw new BusinessException(ErrorCode.OWNER_ROLE_MISSING_INFO);
            }
        }

        // 현재 사용자가 이미 ADMIN 권한을 가지고 있는지 확인
        boolean hasAdminRole = hasUserRole(user, RoleType.ROLE_ADMIN);

        // 요청된 역할 타입으로 변환
        List<RoleType> requestedRoleTypes = roleNames.stream()
                .map(RoleType::fromString)
                .collect(Collectors.toList());

        // ADMIN 권한이 있었다면 유지하기 위해 목록에 추가
        if (hasAdminRole && !requestedRoleTypes.contains(RoleType.ROLE_ADMIN)) {
            requestedRoleTypes.add(RoleType.ROLE_ADMIN);
        }

        // UserRoleService를 사용하여 역할 설정
        setUserRoles(user, requestedRoleTypes);

        log.info("사용자 역할 업데이트 완료 - 이메일: {}, 역할: {}",
                user.getEmail(),
                getUserRoles(user));
    }

}