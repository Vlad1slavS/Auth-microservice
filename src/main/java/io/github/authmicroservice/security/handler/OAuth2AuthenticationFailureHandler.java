package io.github.authmicroservice.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Обработчик неудачной аутентификации OAuth2;
 * Перенаправляет пользователя на страницу с сообщением ошибки.
 */
@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        String errorUrl = UriComponentsBuilder
                .fromUriString("/login")
                .queryParam("error", "oauth_failed")
                .queryParam("message", exception.getMessage())
                .build().toUriString();

        response.sendRedirect(errorUrl);
    }

}

