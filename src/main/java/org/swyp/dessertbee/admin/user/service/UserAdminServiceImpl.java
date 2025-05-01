package org.swyp.dessertbee.admin.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.common.exception.ErrorCode;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.exception.UserExceptions;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

    private final UserRepository userRepository;

    /**
     * 계정 정지(한달)
     */
    @Transactional
    public void suspendUserForOneMonthByUuid(UUID userUuid) {
        UserEntity user = userRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.isSuspended()) {
            throw new UserExceptions.InvalidUserStatusException("이미 정지된 계정입니다.");
        }
        user.suspendForOneMonth();
        userRepository.save(user);
    }
}
