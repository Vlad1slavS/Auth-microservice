package io.github.authmicroservice.model.dto;

import io.github.authmicroservice.model.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO для установки ролей пользователя")
public class UserRolesRequest {

    @Schema(description = "Логин пользователя", example = "testuser")
    @NotBlank
    private String login;

    @Schema(description = "Список ролей пользователя", example = "[\"USER\", \"ADMIN\"]")
    @NotEmpty
    private List<Role.RoleType> roles;

}
