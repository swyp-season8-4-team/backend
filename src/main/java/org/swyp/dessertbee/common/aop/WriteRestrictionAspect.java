package org.swyp.dessertbee.common.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.service.UserService;

@Aspect
@Component
public class WriteRestrictionAspect {

    @Autowired
    private UserService userService; // 현재 사용자 정보를 얻기 위한 서비스

    @Before("@annotation(CheckWriteRestriction)")
    public void checkWriteRestriction() {
        UserEntity user = userService.getCurrentUser();
        if (user.isWriteRestricted()) {
            throw new IllegalStateException("작성 제한 기간입니다. 제한 해제 일시: " + user.getWriteRestrictedUntil());
        }
    }
}
