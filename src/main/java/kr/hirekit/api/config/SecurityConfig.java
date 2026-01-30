package kr.hirekit.api.config;

import kr.hirekit.api.auth.jwt.JwtAuthenticationFilter;
import kr.hirekit.api.auth.oauth2.CustomOAuth2UserService;
import kr.hirekit.api.auth.oauth2.handler.OAuth2SuccessHandler;
import kr.hirekit.api.auth.oauth2.handler.OAuth2FailureHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Spring Security 설정
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    // JWT 인증 필터
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // OAuth2 로그인 시 사용자 정보 처리
    private final CustomOAuth2UserService customOAuth2UserService;

    // 로그인 성공 핸들러
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    // 로그인 실패 핸들러
    private final OAuth2FailureHandler oAuth2FailureHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (REST API이므로)
                .csrf(csrf -> csrf.disable())

                // 세션 사용 안 함 (JWT 사용 예정)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 요청별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 경로
                        .requestMatchers(
                                "/",
                                "/login/**",
                                "/oauth2/**",
                                "/h2-console/**")
                        .permitAll()
                        // 나머지는 인증 필요
                        .anyRequest().authenticated())

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        // 사용자 정보 처리 서비스
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService))
                        // 로그인 성공 시 처리
                        .successHandler(oAuth2SuccessHandler)
                        // 로그인 실패 시 처리
                        .failureHandler(oAuth2FailureHandler))

                // JWT 필터 추가 (UsernamePasswordAuthenticationFilter 앞에)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
