package kr.hirekit.api.config;

import jakarta.servlet.http.HttpServletResponse;
import kr.hirekit.api.auth.jwt.JwtAuthenticationFilter;
import kr.hirekit.api.auth.oauth2.CustomOAuth2UserService;
import kr.hirekit.api.auth.oauth2.filter.OAuth2ReturnToFilter;
import kr.hirekit.api.auth.oauth2.handler.OAuth2FailureHandler;
import kr.hirekit.api.auth.oauth2.handler.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 설정
 * - CORS 설정 (프론트엔드 도메인 허용)
 * - JWT 인증 필터
 * - OAuth2 로그인
 */
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

    // OAuth2 진입 시 return_to 쿠키 저장 필터
    private final OAuth2ReturnToFilter oAuth2ReturnToFilter;

    // 프론트엔드 URL (application.yml에서 주입)
    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // CSRF 비활성화 (REST API이므로)
                .csrf(csrf -> csrf.disable())

                // 세션 사용 안 함 (JWT 사용)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 요청별 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 경로
                        .requestMatchers(
                                "/",
                                "/login/**",
                                "/oauth2/**",
                                "/api/auth/**",
                                "/api/feed",
                                "/api/codes",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/test/**")
                        .permitAll()
                        // 질문·답변 조회만 public (등록은 인증 필요)
                        .requestMatchers(HttpMethod.GET, "/api/questions/**")
                        .permitAll()
                        // 나머지는 인증 필요
                        .anyRequest().authenticated())

                // 미인증 API 요청 시 302 리다이렉트 대신 401 반환
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\":\"Unauthorized\"}");
                        }))

                // OAuth2 로그인 설정
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler))

                // OAuth2 진입 시 return_to 쿠키 저장 (카카오 리다이렉트 전에 실행)
                .addFilterBefore(oAuth2ReturnToFilter, OAuth2AuthorizationRequestRedirectFilter.class)

                // JWT 필터 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS 설정
     * 프론트엔드 도메인에서 API 호출 허용
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 출처 (프론트엔드 URL)
        // 예: https://app.hirekit-dev.kr
        configuration.setAllowedOrigins(List.of(frontendUrl));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // 허용할 요청 헤더
        configuration.setAllowedHeaders(List.of("*"));

        // 쿠키 전송 허용 (중요!)
        // 이게 true여야 쿠키가 cross-origin 요청에 포함됨
        configuration.setAllowCredentials(true);

        // 모든 경로에 CORS 설정 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}