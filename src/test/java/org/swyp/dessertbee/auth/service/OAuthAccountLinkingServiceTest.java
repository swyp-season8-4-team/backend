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
import org.swyp.dessertbee.user.entity.UserEntity;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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
    private OAuthAccountLinkingServiceImpl oAuthAccountLinkingService;

    private OAuth2Response oauth2Response;
    private UserEntity existingUser;
    private AuthEntity existingAuth;

    @BeforeEach
    void setUp() {
        oauth2Response = new OAuth2Response() {
            @Override
            public String getProvider() {
                return "kakao";
            }

            @Override
            public String getProviderId() {
                return "123456789";
            }

            @Override
            public String getEmail() {
                return "test@example.com";
            }

            @Override
            public String getNickname() {
                return "테스트유저";
            }

            @Override
            public String getImageUrl() {
                return "https://example.com/profile.jpg";
            }
        };

        existingUser = UserEntity.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("기존유저")
                .userUuid(UUID.randomUUID())
                .build();

        existingAuth = AuthEntity.builder()
                .id(1)
                .provider("apple")
                .providerId("987654321")
                .user(existingUser)
                .active(true)
                .build();

        existingUser.getAuthEntities().add(existingAuth);
    }

    @Test
    @DisplayName("새로운 사용자 생성이 필요한 경우")
    void shouldCreateNewUserWhenNoExistingUser() {
        // given
        when(userRepository.findByEmailAndOAuthProvider(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        // when
        UserEntity result = oAuthAccountLinkingService.findOrCreateUser(oauth2Response);

        // then
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getNickname()).isEqualTo("테스트유저");
        assertThat(result.getId()).isNull(); // 새로운 사용자는 ID가 null
    }

    @Test
    @DisplayName("동일한 OAuth 제공자로 가입된 사용자가 있는 경우")
    void shouldReturnExistingUserWithSameProvider() {
        // given
        when(userRepository.findByEmailAndOAuthProvider(anyString(), anyString()))
                .thenReturn(Optional.of(existingUser));

        // when
        UserEntity result = oAuthAccountLinkingService.findOrCreateUser(oauth2Response);

        // then
        assertThat(result).isEqualTo(existingUser);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("동일한 이메일로 가입된 기존 사용자가 있는 경우")
    void shouldReturnExistingUserWithSameEmail() {
        // given
        when(userRepository.findByEmailAndOAuthProvider(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.of(existingUser));

        // when
        UserEntity result = oAuthAccountLinkingService.findOrCreateUser(oauth2Response);

        // then
        assertThat(result).isEqualTo(existingUser);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("기존 사용자에게 새로운 OAuth 제공자 자동 연결")
    void shouldLinkOAuthProviderToExistingUser() {
        // given
        when(authRepository.findByProviderAndProviderId(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(authRepository.save(any(AuthEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        UserEntity result = oAuthAccountLinkingService.linkOAuthProviderToUser(
                existingUser, oauth2Response, "device123", false);

        // then
        assertThat(result).isEqualTo(existingUser);
        assertThat(result.getAuthEntities()).hasSize(2);
        assertThat(result.getAuthEntities()).anyMatch(auth -> 
                auth.getProvider().equals("apple") && auth.getProviderId().equals("987654321"));
        assertThat(result.getAuthEntities()).anyMatch(auth -> 
                auth.getProvider().equals("kakao") && auth.getProviderId().equals("123456789"));
    }

    @Test
    @DisplayName("이미 해당 OAuth 제공자로 연결된 계정이 있는 경우")
    void shouldNotLinkWhenProviderAlreadyExists() {
        // given
        when(authRepository.findByProviderAndProviderId(anyString(), anyString()))
                .thenReturn(Optional.of(existingAuth));

        // when
        UserEntity result = oAuthAccountLinkingService.linkOAuthProviderToUser(
                existingUser, oauth2Response, "device123", false);

        // then
        assertThat(result).isEqualTo(existingUser);
        assertThat(result.getAuthEntities()).hasSize(1); // 기존 Apple 계정만 존재
    }

    @Test
    @DisplayName("사용자가 이미 해당 OAuth 제공자로 가입되어 있는 경우")
    void shouldNotLinkWhenUserAlreadyHasProvider() {
        // given
        AuthEntity kakaoAuth = AuthEntity.builder()
                .id(2)
                .provider("kakao")
                .providerId("123456789")
                .user(existingUser)
                .active(true)
                .build();
        existingUser.getAuthEntities().add(kakaoAuth);

        when(authRepository.findByProviderAndProviderId(anyString(), anyString()))
                .thenReturn(Optional.empty());

        // when
        UserEntity result = oAuthAccountLinkingService.linkOAuthProviderToUser(
                existingUser, oauth2Response, "device123", false);

        // then
        assertThat(result).isEqualTo(existingUser);
        assertThat(result.getAuthEntities()).hasSize(2); // 기존 Apple, Kakao 계정만 존재
    }

    @Test
    @DisplayName("계정 연결 실패 시 예외 발생")
    void shouldThrowExceptionWhenAccountLinkingFails() {
        // given
        when(authRepository.findByProviderAndProviderId(anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(authRepository.save(any(AuthEntity.class)))
                .thenThrow(new RuntimeException("Database error"));

        // when & then
        assertThatThrownBy(() -> oAuthAccountLinkingService.linkOAuthProviderToUser(
                existingUser, oauth2Response, "device123", false))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");
    }
} 