package org.swyp.dessertbee.auth.service;

import org.swyp.dessertbee.auth.dto.TokenResponse;

public interface AuthService {
    TokenResponse refreshTokens(String refreshToken);
    void saveRefreshToken(String email, String refreshToken);
    void revokeRefreshToken(String email);
}

