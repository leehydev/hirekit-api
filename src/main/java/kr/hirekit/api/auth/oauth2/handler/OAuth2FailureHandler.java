package kr.hirekit.api.auth.oauth2.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    // application.yml에서 주입
    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {

        log.error("OAuth2 로그인 실패 - 원인: {}", exception.getMessage());

        String errorMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
        String redirectUrl = frontendUrl + "/oauth/callback?error=" + errorMessage;

        response.sendRedirect(redirectUrl);
    }
}