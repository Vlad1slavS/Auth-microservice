package io.github.authmicroservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "DTO для регистрации нового пользователя")
public class SignupRequest {

    @Schema(description = "Логин пользователя", example = "newuser")
    @NotBlank(message = "Login is required")
    @Size(min = 3, max = 50, message = "Login must be between 3 and 50 characters")
    private String login;

    @Schema(description = "Пароль пользователя", example = "password123")
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @Schema(description = "Email пользователя", example = "test@mail.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

}
