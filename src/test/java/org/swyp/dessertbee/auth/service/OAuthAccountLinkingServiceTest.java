package org.swyp.dessertbee.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.swyp.dessertbee.auth.entity.AuthEntity;
import org.swyp.dessertbee.auth.oauth2.OAuth2Response;
import org.swyp.dessertbee.auth.repository.AuthRepository;
import org.swyp.dessertbee.common.exception.BusinessException;
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * OAuthAccountLinkingService 단위 테스트
 * 자동 계정 연결 기능을 테스트합니다.
 */
@ExtendWith(MockitoExtension.class)
class OAuthAccountLinkingServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthRepository authRepository;

    @InjectMocks
    private OAuthAccountLinkingService oAuthAccountLinkingService;

    private UserEntity existingUser;
    private OAuth2Response appleOAuthResponse;
    private OAuth2Response kakaoOAuthResponse;
    private AuthEntity appleAuth;

    @BeforeEach
    void setUp() {
        // 기존 사용자 설정 (Apple로 가입된 사용자)
        existingUser = UserEntity.builder()
                .id(1L)
                .userUuid(UUID.randomUUID())
                .email("test@example.com")
                .nickname("테스트사용자")
                .build();

        // Apple OAuth 응답
        appleOAuthResponse = new OAuth2Response() {
            @Override
            public String getProvider() {
                return "apple";
            }

            @Override
            public String getProviderId() {
                return "apple_123456";
            }

            @Override
            public String getEmail() {
                return "test@example.com";
            }

            @Override
            public String getNickname() {
                return "테스트사용자";
            }

            @Override
            public String getImageUrl() {
                return null;
            }
        };

        // Kakao OAuth 응답
        kakaoOAuthResponse = new OAuth2Response() {
            @Override
            public String getProvider() {
                return "kakao";
            }

            @Override
            public String getProviderId() {
                return "kakao_789012";
            }

            @Override
            public String getEmail() {
                return "test@example.com";
            }

            @Override
            public String getNickname() {
                return "테스트사용자";
            }

            @Override
            public String getImageUrl() {
                return null;
            }
        };

        // Apple Auth 엔티티
        appleAuth = AuthEntity.builder()
                .id(1)
                .user(existingUser)
                .provider("apple")
                .providerId("apple_123456")
                .deviceId("device_apple")
                .active(true)
                .build();

        existingUser.getAuthEntities().add(appleAuth);
    }

    @Test
    @DisplayName("동일한 OAuth 제공자로 가입된 사용자 발견 시 기존 사용자 반환")
    void shouldReturnExistingUserWhenSameProviderExists() {
        // given
        when(userRepository.findByEmailAndOAuthProvider("test@example.com", "apple"))
                .thenReturn(Optional.of(existingUser));

        // when
        UserEntity result = oAuthAccountLinkingService.processOAuthUserLogin(appleOAuthResponse, "device_apple", true);

        // then
        assertThat(result).isEqualTo(existingUser);
        assertThat(result.getId()).isEqualTo(1L);
        verify(userRepository, times(1)).findByEmailAndOAuthProvider("test@example.com", "apple");
        verify(userRepository, never()).findByEmail(anyString());
        verify(authRepository, never()).save(any(AuthEntity.class));
    }

    @Test
    @DisplayName("동일한 이메일로 다른 OAuth 제공자 가입 시 자동 계정 연결")
    void shouldAutomaticallyLinkAccountWhenDifferentProviderExists() {
        // given
        when(userRepository.findByEmailAndOAuthProvider("test@example.com", "kakao"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.findOAuthProvidersByEmail("test@example.com"))
                .thenReturn(Arrays.asList("apple"));
        when(authRepository.findByProviderAndProviderId("kakao", "kakao_789012"))
                .thenReturn(Optional.empty());
        when(authRepository.save(any(AuthEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        UserEntity result = oAuthAccountLinkingService.processOAuthUserLogin(kakaoOAuthResponse, "device_kakao", true);

        // then
        assertThat(result).isEqualTo(existingUser);
        assertThat(result.getAuthEntities()).hasSize(2);
        assertThat(result.getAuthEntities()).anyMatch(auth -> 
                auth.getProvider().equals("apple") && auth.getProviderId().equals("apple_123456"));
        assertThat(result.getAuthEntities()).anyMatch(auth -> 
                auth.getProvider().equals("kakao") && auth.getProviderId().equals("kakao_789012"));
        
        verify(authRepository, times(1)).save(any(AuthEntity.class));
        assertThat(oAuthAccountLinkingService.isAccountLinkingOccurred()).isTrue();
    }

    @Test
    @DisplayName("새로운 사용자 가입 시 ID가 null인 UserEntity 반환")
    void shouldReturnNewUserWhenNoExistingUserFound() {
        // given
        when(userRepository.findByEmailAndOAuthProvider("new@example.com", "apple"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@example.com"))
                .thenReturn(Optional.empty());

        OAuth2Response newUserOAuthResponse = new OAuth2Response() {
            @Override
            public String getProvider() {
                return "apple";
            }

            @Override
            public String getProviderId() {
                return "apple_new";
            }

            @Override
            public String getEmail() {
                return "new@example.com";
            }

            @Override
            public String getNickname() {
                return "새사용자";
            }

            @Override
            public String getImageUrl() {
                return null;
            }
        };

        // when
        UserEntity result = oAuthAccountLinkingService.processOAuthUserLogin(newUserOAuthResponse, "device_new", true);

        // then
        assertThat(result.getId()).isNull();
        assertThat(result.getEmail()).isEqualTo("new@example.com");
        assertThat(result.getNickname()).isEqualTo("새사용자");
        verify(authRepository, never()).save(any(AuthEntity.class));
        assertThat(oAuthAccountLinkingService.isAccountLinkingOccurred()).isFalse();
    }

    @Test
    @DisplayName("이미 연결된 OAuth 제공자인 경우 중복 연결 방지")
    void shouldPreventDuplicateLinkingWhenProviderAlreadyExists() {
        // given
        when(userRepository.findByEmailAndOAuthProvider("test@example.com", "apple"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.findOAuthProvidersByEmail("test@example.com"))
                .thenReturn(Arrays.asList("apple"));

        // when
        UserEntity result = oAuthAccountLinkingService.processOAuthUserLogin(appleOAuthResponse, "device_apple", true);

        // then
        assertThat(result).isEqualTo(existingUser);
        assertThat(result.getAuthEntities()).hasSize(1); // 기존 Apple 계정만 존재
        verify(authRepository, never()).save(any(AuthEntity.class));
        assertThat(oAuthAccountLinkingService.isAccountLinkingOccurred()).isFalse();
    }

    @Test
    @DisplayName("계정 연결 실패 시 예외 발생")
    void shouldThrowExceptionWhenAccountLinkingFails() {
        // given
        when(userRepository.findByEmailAndOAuthProvider("test@example.com", "kakao"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.findOAuthProvidersByEmail("test@example.com"))
                .thenReturn(Arrays.asList("apple"));
        when(authRepository.findByProviderAndProviderId("kakao", "kakao_789012"))
                .thenReturn(Optional.empty());
        when(authRepository.save(any(AuthEntity.class)))
                .thenThrow(new RuntimeException("Database error"));

        // when & then
        assertThatThrownBy(() -> 
                oAuthAccountLinkingService.processOAuthUserLogin(kakaoOAuthResponse, "device_kakao", true))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", org.swyp.dessertbee.common.exception.ErrorCode.OAUTH_ACCOUNT_LINKING_FAILED);
    }

    @Test
    @DisplayName("사용자의 OAuth 제공자 목록 조회")
    void shouldGetUserOAuthProviders() {
        // given
        when(userRepository.findOAuthProvidersByEmail("test@example.com"))
                .thenReturn(Arrays.asList("apple", "kakao"));

        // when
        List<String> providers = oAuthAccountLinkingService.getUserOAuthProviders("test@example.com");

        // then
        assertThat(providers).containsExactly("apple", "kakao");
    }

    @Test
    @DisplayName("사용자의 OAuth 계정 수 조회")
    void shouldGetOAuthProviderCount() {
        // given
        when(userRepository.countOAuthProvidersByEmail("test@example.com"))
                .thenReturn(2L);

        // when
        long count = oAuthAccountLinkingService.getOAuthProviderCount("test@example.com");

        // then
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("계정 연결 상태 초기화")
    void shouldResetAccountLinkingStatus() {
        // given
        when(userRepository.findByEmailAndOAuthProvider("test@example.com", "kakao"))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(existingUser));
        when(userRepository.findOAuthProvidersByEmail("test@example.com"))
                .thenReturn(Arrays.asList("apple"));
        when(authRepository.findByProviderAndProviderId("kakao", "kakao_789012"))
                .thenReturn(Optional.empty());
        when(authRepository.save(any(AuthEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        oAuthAccountLinkingService.processOAuthUserLogin(kakaoOAuthResponse, "device_kakao", true);
        assertThat(oAuthAccountLinkingService.isAccountLinkingOccurred()).isTrue();
        
        oAuthAccountLinkingService.resetAccountLinkingStatus();
        
        // then
        assertThat(oAuthAccountLinkingService.isAccountLinkingOccurred()).isFalse();
    }

    @Test
    @DisplayName("계정 연결 정보 조회")
    void shouldGetAccountLinkingInfo() {
        // given
        when(userRepository.findOAuthProvidersByEmail("test@example.com"))
                .thenReturn(Arrays.asList("apple", "kakao"));
        when(userRepository.countOAuthProvidersByEmail("test@example.com"))
                .thenReturn(2L);

        // when
        var info = oAuthAccountLinkingService.getAccountLinkingInfo("test@example.com");

        // then
        assertThat(info.get("email")).isEqualTo("test@example.com");
        assertThat((List<String>) info.get("providers")).containsExactly("apple", "kakao");
        assertThat(info.get("providerCount")).isEqualTo(2L);
        assertThat(info.get("hasMultipleProviders")).isEqualTo(true);
    }
} 