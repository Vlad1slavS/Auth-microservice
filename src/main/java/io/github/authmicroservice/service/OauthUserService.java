package io.github.authmicroservice.service;

import io.github.authmicroservice.model.dto.GitHubLoginInfo;
import io.github.authmicroservice.model.dto.GoogleLoginInfo;
import io.github.authmicroservice.model.entity.Role;
import io.github.authmicroservice.model.entity.User;
import io.github.authmicroservice.model.entity.UserRole;
import io.github.authmicroservice.model.enums.Provider;
import io.github.authmicroservice.repository.RoleRepository;
import io.github.authmicroservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для работы с oauth пользователями
 */
@Service
public class OauthUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public OauthUserService(UserRepository userRepository,
                       RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public User processGoogleOAuthUser(GoogleLoginInfo googleUserInfo) {

        Optional<User> existingUser = userRepository.findByEmailWithRoles(googleUserInfo.getEmail());

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            if (user.getProvider() == Provider.LOCAL) {
                throw new IllegalStateException("User with email " + googleUserInfo.getEmail() +
                        " was registered locally. Cannot authenticate via Google OAuth2.");
            }

            user.setFullName(googleUserInfo.getName());
            user.setProfilePictureUrl(googleUserInfo.getPicture());
            user.setGoogleId(googleUserInfo.getId());
            return userRepository.save(user);

        } else {

            Role userRole = checkUserRoleExists();

            String login = googleUserInfo.getEmail().split("@")[0];

            User user = User.builder()
                    .login(login)
                    .email(googleUserInfo.getEmail())
                    .password(null)
                    .active(true)
                    .provider(Provider.GOOGLE)
                    .googleId(googleUserInfo.getId())
                    .fullName(googleUserInfo.getName())
                    .profilePictureUrl(googleUserInfo.getPicture())
                    .build();

            user = userRepository.save(user);

            UserRole userRoleEntity = UserRole.builder()
                    .user(user)
                    .role(userRole)
                    .build();

            user.setRoles(new ArrayList<>(List.of(userRoleEntity)));
            return userRepository.save(user);

        }
    }

    @Transactional
    public User processGitHubOAuthUser(GitHubLoginInfo gitHubUserInfo) {

        Optional<User> existingUser = userRepository.findByGitHubIdWithRoles(gitHubUserInfo.getId());

        String newEmail = gitHubUserInfo.getEmail();

        if (newEmail == null) {
            newEmail = gitHubUserInfo.getLogin() + "_" + gitHubUserInfo.getId() + "@noreply.github.oauth";
        }

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            if (user.getProvider() == Provider.LOCAL) {
                throw new IllegalStateException("User with GitHub ID " + gitHubUserInfo.getId() +
                        " was registered locally. Cannot authenticate via GitHub OAuth2.");
            }

            user.setFullName(gitHubUserInfo.getName());
            user.setProfilePictureUrl(gitHubUserInfo.getAvatarUrl());
            user.setGithubId(gitHubUserInfo.getId());
            return userRepository.save(user);

        } else {

            Role userRole = checkUserRoleExists();

            User user = User.builder()
                    .login(gitHubUserInfo.getLogin())
                    .email(newEmail)
                    .githubId(gitHubUserInfo.getId())
                    .password(null)
                    .active(true)
                    .provider(Provider.GITHUB)
                    .fullName(gitHubUserInfo.getName())
                    .profilePictureUrl(gitHubUserInfo.getAvatarUrl())
                    .build();

            user = userRepository.save(user);

            UserRole userRoleEntity = UserRole.builder()
                    .user(user)
                    .role(userRole)
                    .build();

            user.setRoles(new ArrayList<>(List.of(userRoleEntity)));
            return userRepository.save(user);

        }
    }

    private Role checkUserRoleExists() {
        Role userRole = roleRepository.findById(Role.RoleType.USER)
                .orElseThrow(() -> new IllegalStateException("USER role not found"));

        return userRole;
    }

}
