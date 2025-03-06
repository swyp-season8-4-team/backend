package org.swyp.dessertbee.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.swyp.dessertbee.auth.jwt.JWTFilter;
import org.swyp.dessertbee.auth.jwt.JWTUtil;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.swyp.dessertbee.user.repository.UserRepository;

import java.util.Arrays;
import java.util.Collections;

/**
 * 스프링 시큐리티 설정 클래스
 * OAuth2 및 JWT 인증 설정 포함
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

//    @Value("${spring.graphql.cors.allowed-origins}")
//    private String corsAllowedOrigins;

    @Bean
    public JWTFilter jwtFilter() {
        return new JWTFilter(jwtUtil, userRepository);
    }

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
//                        // 다른 public API 엔드포인트
//                        .requestMatchers("/api/auth/**").permitAll()
//                        .requestMatchers("/api/public/**").permitAll()
//                        // 나머지 요청은 인증 필요
//                        .anyRequest().authenticated()
                                // OAuth2 인증이 필요한 엔드포인트만 지정
                                .requestMatchers("/api/oauth2/authorization/**").authenticated()
                                .requestMatchers("/api/oauth2/code/**").authenticated()
                                // 나머지 모든 요청 허용
                                .anyRequest().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint ->
                                endpoint.baseUri("/api/oauth2/authorization"))
                        .loginProcessingUrl("/api/oauth2/code")
                )
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);
//        // CORS 설정 추가
//        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        // CORS 설정 비활성화 (NGINX에서 처리)
        http.cors(AbstractHttpConfigurer::disable);

        return http.build();
    }


//    /**
//     * CORS 설정
//     */
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//
//        // allowedOrigins를 yml 설정값에서 가져와서 설정
//        String[] origins = corsAllowedOrigins.split(",");
//        configuration.setAllowedOrigins(Arrays.asList(origins)); // 명시적으로 허용할 도메인 지정
//
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
//        configuration.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization", "X-Email-Verification-Token"));
//        configuration.setAllowCredentials(true); // 쿠키를 포함한 크로스 도메인 요청 허용
//        configuration.setMaxAge(3600L);
//
//        // 노출할 헤더 설정 - Set-Cookie도 포함
//        configuration.setExposedHeaders(Arrays.asList("Authorization", "Set-Cookie","X-Email-Verification-Token"));
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//
//        return source;
//    }

    /**
     * 비밀번호 인코더 설정
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
