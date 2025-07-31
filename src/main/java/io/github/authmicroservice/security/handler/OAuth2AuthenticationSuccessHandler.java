package io.github.authmicroservice.security.handler;

import io.github.authmicroservice.model.dto.GitHubLoginInfo;
import io.github.authmicroservice.model.dto.GoogleLoginInfo;
import io.github.authmicroservice.model.entity.User;
import io.github.authmicroservice.service.GitHubApiService;
import io.github.authmicroservice.service.JwtService;
import io.github.authmicroservice.service.OauthUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Обработчик успешной аутентификации OAuth2;
 * Перенаправляет пользователя на страницу с JWT токеном.
 */
@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final OauthUserService oauthUserService;
    private final GitHubApiService gitHubApiService;

    public OAuth2AuthenticationSuccessHandler(JwtService jwtService, OauthUserService oauthUserService,
                                              GitHubApiService gitHubApiService) {
        this.jwtService = jwtService;
        this.oauthUserService = oauthUserService;
        this.gitHubApiService = gitHubApiService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
            String registrationId = ((OAuth2AuthenticationToken) authentication)
                    .getAuthorizedClientRegistrationId();

            User user;


            if ("google".equals(registrationId)) {

                GoogleLoginInfo googleUserInfo = GoogleLoginInfo.builder()
                        .id(oauth2User.getAttribute("sub"))
                        .email(oauth2User.getAttribute("email"))
                        .name(oauth2User.getAttribute("name"))
                        .picture(oauth2User.getAttribute("picture"))
                        .build();

                user = oauthUserService.processGoogleOAuthUser(googleUserInfo);

            } else if ("github".equals(registrationId)) {

                Integer id = oauth2User.getAttribute("id");

                String email = oauth2User.getAttribute("email");

                if (email == null) {
                    Optional<String> apiEmail = gitHubApiService.getPrimaryEmail(token);
                    if (apiEmail.isPresent()) {
                        email = apiEmail.get();
                    }
                }

                GitHubLoginInfo gitHubUserInfo = GitHubLoginInfo.builder()
                        .id(String.valueOf(id))
                        .login(oauth2User.getAttribute("login"))
                        .name(oauth2User.getAttribute("name"))
                        .email(null)
                        .avatarUrl(oauth2User.getAttribute("avatar_url"))
                        .build();

                user = oauthUserService.processGitHubOAuthUser(gitHubUserInfo);

            } else {

                throw new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId);

            }

            String jwt = jwtService.generateToken(user);

            String redirectUrl = UriComponentsBuilder
                    .fromUriString("/oauth2/redirect")
                    .queryParam("token", jwt)
                    .build(true)
                    .toUriString();

            response.sendRedirect(redirectUrl);

        } catch (Exception e) {

            String error = URLEncoder.encode(e.getMessage()
                    .replaceAll("[\r\n]", " "), StandardCharsets.UTF_8);

            String errorUrl = UriComponentsBuilder
                    .fromUriString("/login")
                    .queryParam("error", "oauth_failed")
                    .queryParam("message", error)
                    .build(true)
                    .toUriString();

            response.sendRedirect(errorUrl);
        }
    }

}
