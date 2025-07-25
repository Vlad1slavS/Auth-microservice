package io.github.authmicroservice.service;

import io.github.authmicroservice.model.dto.UserRolesRequest;
import io.github.authmicroservice.model.entity.Role;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface UserRoleService {

    void saveUserRoles(UserRolesRequest request);

    List<Role.RoleType> getUserRoles(String login) throws AccessDeniedException;

}
