package org.swyp.dessertbee.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.swyp.dessertbee.auth.jwt.JWTFilter;
import org.swyp.dessertbee.auth.jwt.JWTUtil;
import org.swyp.dessertbee.auth.oauth2.CustomSuccessHandler;
import org.swyp.dessertbee.auth.service.AuthService;
import org.swyp.dessertbee.auth.service.CustomOAuth2UserService;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
/**
 * 스프링 시큐리티 설정 클래스
 * OAuth2 및 JWT 인증 설정 포함
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JWTUtil jwtUtil;
    private final AuthService authService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
//                        // OAuth2 관련 엔드포인트 명확히 지정
//                        .requestMatchers("/api/oauth2/authorization").permitAll()
//                        .requestMatchers("/api/oauth2/code").permitAll()
//                        .requestMatchers("/login/oauth2/code/kakao").permitAll()  // 카카오 콜백 URL
//                        // 다른 public API 엔드포인트
//                        .requestMatchers("/api/auth/**").permitAll()
//                        .requestMatchers("/api/public/**").permitAll()
//                        // 나머지 요청은 인증 필요
//                        .anyRequest().authenticated()
                                // OAuth2 인증이 필요한 엔드포인트만 지정
                                .requestMatchers("/api/oauth2/authorization/**").authenticated()
                                // 나머지 모든 요청 허용
                                .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo ->
                                userInfo.userService(customOAuth2UserService))
                        .successHandler(customSuccessHandler)
                )
                .addFilterBefore(new JWTFilter(jwtUtil, authService),
                        UsernamePasswordAuthenticationFilter.class);

        // CORS 설정 추가
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        return http.build();
    }


    /**
     * CORS 설정
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * 비밀번호 인코더 설정
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}