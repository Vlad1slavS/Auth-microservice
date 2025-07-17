package io.github.authmicroservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO для ответа после успешной аутентификации")
public class JwtResponse {

    @Schema(description = "JWT токен")
    private String token;

    @Schema(description = "Логин пользователя", example = "testuser")
    private String login;

    @Schema(description = "Роли пользователя", example = "[\"ROLE_USER\", \"ROLE_ADMIN\"]")
    private List<String> roles;

}
