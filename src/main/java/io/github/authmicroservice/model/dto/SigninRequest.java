package io.github.authmicroservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для запроса на вход в систему")
public class SigninRequest {

    @Schema(description = "Логин пользователя", example = "testuser")
    @NotBlank
    private String login;

    @Schema(description = "Пароль пользователя", example = "password123")
    @NotBlank
    private String password;

}
