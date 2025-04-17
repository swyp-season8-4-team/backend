package org.swyp.dessertbee.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.role.entity.RoleEntity;
import org.swyp.dessertbee.role.entity.RoleType;
import org.swyp.dessertbee.role.repository.RoleRepository;
import org.swyp.dessertbee.role.service.RoleService;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * 개발 및 테스트 환경에서 테스트 계정을 자동으로 생성하는 컴포넌트
 * 프로필이 dev 또는 test인 경우에만 활성화됨
 */
@Component
@Profile({"dev", "release"})
@RequiredArgsConstructor
@Slf4j
public class TestAccountInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    // 테스트 계정 비밀번호
    private static final String USER_PASSWORD = "Test1234!";
    private static final String OWNER_PASSWORD = "Test1234!";
    private static final String ADMIN_PASSWORD = "Admin1234!";

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("🔧 테스트 계정 초기화 시작...");

        // 필수 역할이 존재하는지 확인하고 없으면 생성
        ensureRolesExist();

        // 테스트 계정 생성
        createTestUser();
        createTestOwner();
        createTestAdmin();

        log.info("테스트 계정 생성 및 초기화 완료");
    }

    /**
     * 기본 역할(USER, OWNER, ADMIN)이 존재하는지 확인하고 없으면 생성
     */
    private void ensureRolesExist() {
        for (RoleType roleType : RoleType.values()) {
            if (!roleRepository.existsByName(roleType)) {
                RoleEntity role = RoleEntity.builder()
                        .name(roleType)
                        .build();
                roleRepository.save(role);
                log.info("역할 생성: {}", roleType.getRoleName());
            }
        }
    }

    /**
     * 일반 사용자 테스트 계정 생성
     */
    private void createTestUser() {
        String email = "user@test.com";
        Optional<UserEntity> existingUser = userRepository.findByEmail(email);

        if (existingUser.isEmpty()) {
            UserEntity user = UserEntity.builder()
                    .email(email)
                    .password(passwordEncoder.encode(USER_PASSWORD))
                    .name("테스트사용자")
                    .nickname("테스트유저")
                    .phoneNumber("010-1234-5678")
                    .userUuid(UUID.randomUUID())
                    .build();

            userRepository.save(user);

            // 사용자 역할 추가
            RoleEntity userRole = roleService.findRoleByType(RoleType.ROLE_USER);
            user.addRole(userRole);

            log.info("테스트 일반 사용자 계정 생성 완료: {}", email);
        } else {
            // 기존 계정 비밀번호 업데이트
            UserEntity user = existingUser.get();
            user.updatePassword(passwordEncoder.encode(USER_PASSWORD));
            userRepository.save(user);
            log.info("테스트 일반 사용자 계정 비밀번호 업데이트: {}", email);
        }
    }

    /**
     * 사업자 테스트 계정 생성
     */
    private void createTestOwner() {
        String email = "owner@test.com";
        Optional<UserEntity> existingUser = userRepository.findByEmail(email);

        if (existingUser.isEmpty()) {
            UserEntity owner = UserEntity.builder()
                    .email(email)
                    .password(passwordEncoder.encode(OWNER_PASSWORD))
                    .name("테스트사업자")
                    .nickname("테스트오너")
                    .phoneNumber("010-2345-6789")
                    .address("서울시 강남구 테헤란로 123")
                    .userUuid(UUID.randomUUID())
                    .build();

            userRepository.save(owner);

            // 사용자 역할과 사업자 역할 추가
            RoleEntity userRole = roleService.findRoleByType(RoleType.ROLE_USER);
            RoleEntity ownerRole = roleService.findRoleByType(RoleType.ROLE_OWNER);
            owner.addRole(userRole);
            owner.addRole(ownerRole);

            log.info("테스트 사업자 계정 생성 완료: {}", email);
        } else {
            // 기존 계정 비밀번호 업데이트
            UserEntity owner = existingUser.get();
            owner.updatePassword(passwordEncoder.encode(OWNER_PASSWORD));
            userRepository.save(owner);
            log.info("테스트 사업자 계정 비밀번호 업데이트: {}", email);
        }
    }

    /**
     * 관리자 테스트 계정 생성
     */
    private void createTestAdmin() {
        String email = "admin@test.com";
        Optional<UserEntity> existingUser = userRepository.findByEmail(email);

        if (existingUser.isEmpty()) {
            UserEntity admin = UserEntity.builder()
                    .email(email)
                    .password(passwordEncoder.encode(ADMIN_PASSWORD))
                    .name("테스트관리자")
                    .nickname("어드민")
                    .phoneNumber("010-3456-7890")
                    .userUuid(UUID.randomUUID())
                    .build();

            userRepository.save(admin);

            // 사용자 역할과 관리자 역할 추가
            RoleEntity userRole = roleService.findRoleByType(RoleType.ROLE_USER);
            RoleEntity adminRole = roleService.findRoleByType(RoleType.ROLE_ADMIN);
            admin.addRole(userRole);
            admin.addRole(adminRole);

            log.info("테스트 관리자 계정 생성 완료: {}", email);
        } else {
            // 기존 계정 비밀번호 업데이트
            UserEntity admin = existingUser.get();
            admin.updatePassword(passwordEncoder.encode(ADMIN_PASSWORD));
            userRepository.save(admin);
            log.info("테스트 관리자 계정 비밀번호 업데이트: {}", email);
        }
    }
}