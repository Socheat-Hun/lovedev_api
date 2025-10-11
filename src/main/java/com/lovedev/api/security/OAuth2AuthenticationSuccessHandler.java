package com.lovedev.api.security;

import com.lovedev.api.service.OAuth2Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Custom OAuth2 success handler
 * Processes OAuth2 login and redirects to frontend with tokens
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuth2Service oAuth2Service;

    @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/redirect}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        if (!(authentication instanceof OAuth2AuthenticationToken)) {
            log.warn("Authentication is not OAuth2AuthenticationToken");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid authentication type");
            return;
        }

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();

        try {
            // Process OAuth2 login/registration
            OAuth2Service.OAuth2AuthResult result = oAuth2Service.processOAuth2Login(oAuth2User, registrationId);

            // Build redirect URL with tokens
            String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("token", result.getAccessToken())
                    .queryParam("refreshToken", result.getRefreshToken())
                    .queryParam("expiresIn", result.getExpiresIn())
                    .queryParam("isNewUser", result.isNewUser())
                    .build()
                    .toUriString();

            log.info("OAuth2 authentication successful, redirecting to: {}", redirectUri);
            getRedirectStrategy().sendRedirect(request, response, targetUrl);

        } catch (Exception e) {
            log.error("OAuth2 authentication failed", e);

            // Redirect to error page
            String errorUrl = UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("error", "oauth2_failed")
                    .queryParam("message", e.getMessage())
                    .build()
                    .toUriString();

            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }
}