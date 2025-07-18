package io.github.authmicroservice.service;

import io.github.authmicroservice.model.dto.UserRolesRequest;
import io.github.authmicroservice.model.entity.Role;
import io.github.authmicroservice.model.entity.User;
import io.github.authmicroservice.model.entity.UserRole;
import io.github.authmicroservice.repository.RoleRepository;
import io.github.authmicroservice.repository.UserRepository;
import io.github.authmicroservice.repository.UserRoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация сервиса управления ролями пользователей
 */
@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final UserRoleRepository userRoleRepository;

    @Override
    @Transactional
    public void saveUserRoles(UserRolesRequest request) {

        List<Role.RoleType> roleTypes = request.getRoles();
        List<UserRole> newRoles = new ArrayList<>();

        User user = userRepository.findByLogin(request.getLogin())
                .orElseThrow(() -> new EntityNotFoundException("User not found with login: " + request.getLogin()));

        List<UserRole> existingRoles = userRoleRepository.findByUser(user);

        userRoleRepository.deleteAll(existingRoles);

        for (Role.RoleType roleType : roleTypes) {
            Role role = roleRepository.findById(roleType)
                    .orElseThrow(() -> new EntityNotFoundException("Role not found: " + roleType));

            UserRole userRole = UserRole.builder()
                    .user(user)
                    .role(role)
                    .build();

            newRoles.add(userRole);
        }

        userRoleRepository.saveAll(newRoles);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role.RoleType> getUserRoles(String login) throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserLogin = authentication.getName();

        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        boolean isAdmin = authorities.contains("ROLE_ADMIN");

        if (!isAdmin && !currentUserLogin.equals(login)) {
            throw new AccessDeniedException("Access denied. You can only view your own roles");
        }

        if (!userRepository.existsById(login)) {
            throw new EntityNotFoundException("User not found with login: " + login);
        }

        return userRoleRepository.findRoleTypesByUserLogin(login);
    }

}
