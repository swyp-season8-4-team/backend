package org.swyp.dessertbee.store.coupon.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.swyp.dessertbee.store.coupon.repository.UserCouponRepository;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class CouponCodeGenerator {

    private static final int CODE_LENGTH = 6;
    private static final String CODE_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private final UserCouponRepository userCouponRepository;

    public String generateUniqueCouponCode() {
        String code;
        do {
            code = generateRandomCode(CODE_LENGTH);
        } while (userCouponRepository.existsByCouponCode(code)); // 중복 확인
        return code;
    }

    private String generateRandomCode(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CODE_CHARACTERS.length());
            sb.append(CODE_CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}
