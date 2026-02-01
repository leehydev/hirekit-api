package kr.hirekit.api.auth.oauth2.handler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kr.hirekit.api.auth.oauth2.filter.OAuth2ReturnToFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.cookie-domain}")
    private String cookieDomain;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {

        log.error("OAuth2 로그인 실패 - 원인: {}", exception.getMessage());

        String errorMessage = URLEncoder.encode(exception.getMessage(), StandardCharsets.UTF_8);
        String base = frontendUrl + "/oauth/callback?error=" + errorMessage;

        String returnTo = getReturnToFromCookie(request);
        if (returnTo != null && !returnTo.isBlank()) {
            clearReturnToCookie(response);
            base = base + "&return_to=" + URLEncoder.encode(returnTo, StandardCharsets.UTF_8);
        }

        response.sendRedirect(base);
    }

    private String getReturnToFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies)
                .filter(c -> OAuth2ReturnToFilter.COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private void clearReturnToCookie(HttpServletResponse response) {
        String cookieValue = String.format(
                "%s=; Max-Age=0; Path=/; Domain=%s; SameSite=Lax; Secure; HttpOnly",
                OAuth2ReturnToFilter.COOKIE_NAME, cookieDomain
        );
        response.addHeader("Set-Cookie", cookieValue);
    }
}