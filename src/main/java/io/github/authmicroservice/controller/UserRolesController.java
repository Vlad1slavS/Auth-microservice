package io.github.authmicroservice.controller;

import io.github.authmicroservice.model.dto.UserRolesRequest;
import io.github.authmicroservice.model.entity.Role;
import io.github.authmicroservice.service.UserRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("api/v1/user-roles")
@Tag(name = "User Roles", description = "API для управления ролями пользователей")

public class UserRolesController {

    private final UserRoleService userRoleService;

    public UserRolesController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    @Operation(summary = "Сохранение ролей пользователя")
    @ApiResponse(responseCode = "200", description = "Роли успешно сохранены")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/save")
    public ResponseEntity<Void> saveUserRoles(@Valid @RequestBody UserRolesRequest request) {
        userRoleService.saveUserRoles(request);
        return ResponseEntity.ok().build();

    }

    @Operation(summary = "Получение ролей пользователя")
    @ApiResponse(
            responseCode = "200",
            description = "Список ролей пользователя",
            content = @Content(schema = @Schema(implementation = Role.RoleType.class))
    )
    @GetMapping("/{login}")
    public ResponseEntity<?> getUserRoles(@PathVariable String login) throws AccessDeniedException {
        return ResponseEntity.ok(userRoleService.getUserRoles(login));
    }

}


