package io.github.authmicroservice.model.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO для хранения информации о пользователе GitHub при OAuth авторизации
 */
@Data
@Builder
public class GitHubLoginInfo {

    private String id;

    private String login;

    private String name;

    private String email;

    private String avatarUrl;

}
