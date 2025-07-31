package io.github.authmicroservice.service;

import io.github.authmicroservice.model.dto.GitHubLoginInfo;
import io.github.authmicroservice.model.dto.GoogleLoginInfo;
import io.github.authmicroservice.model.dto.JwtResponse;
import io.github.authmicroservice.model.dto.SigninRequest;
import io.github.authmicroservice.model.dto.SignupRequest;

public interface AuthService {

    /**
     * Регистрация нового пользователя
     */
    void signup(SignupRequest request);

    /**
     * Авторизация пользователя
     * @return JWT токен
     */
    JwtResponse signin(SigninRequest request);

    /**
     * Процесс OAuth авторизации через Google
     */
    JwtResponse googleOauthLoginProcess(GoogleLoginInfo googleUserInfo);

    /**
     * Процесс OAuth авторизации через GitHub
     */
    JwtResponse githubOauthLoginProcess(GitHubLoginInfo gitHubUserInfo);

}
