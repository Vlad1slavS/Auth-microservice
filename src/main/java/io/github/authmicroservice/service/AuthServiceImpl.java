package io.github.authmicroservice.service;

import io.github.authmicroservice.model.dto.GitHubLoginInfo;
import io.github.authmicroservice.model.dto.GoogleLoginInfo;
import io.github.authmicroservice.model.dto.JwtResponse;
import io.github.authmicroservice.model.dto.SigninRequest;
import io.github.authmicroservice.model.dto.SignupRequest;
import io.github.authmicroservice.model.entity.Role;
import io.github.authmicroservice.model.entity.User;
import io.github.authmicroservice.model.entity.UserRole;
import io.github.authmicroservice.model.enums.Provider;
import io.github.authmicroservice.repository.RoleRepository;
import io.github.authmicroservice.repository.UserRepository;
import io.github.authmicroservice.repository.UserRoleRepository;
import io.github.authmicroservice.security.encoder.CustomPasswordEncoder;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Реализация сервиса аутентификации и регистрации пользователей
 */
@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CustomPasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserRepository userRepository,
                           CustomPasswordEncoder passwordEncoder,
                           RoleRepository roleRepository,
                           UserRoleRepository userRoleRepository,
                           JwtService jwtService,
                           AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    @Transactional
    public void signup(SignupRequest request) {

        log.debug("Attempting to register user: {}", request.getLogin());

        if (userRepository.existsById(request.getLogin())) {
            throw new EntityExistsException("User with login " + request.getLogin() + " already exists");
        }

        if (userRepository.existsByEmailAndProvider(request.getEmail(), Provider.LOCAL)) {
            throw new EntityExistsException("User with email " + request.getEmail() + " already exists");
        }

        User user = User.builder()
                .login(request.getLogin())
                .email(request.getEmail())
                .provider(Provider.LOCAL)
                .password(passwordEncoder.encode(request.getPassword(), request.getLogin(), request.getEmail()))
                .active(true)
                .build();

        log.debug("Save user: {}", user.getLogin());

        userRepository.save(user);

        Role userRole = roleRepository.findById(Role.RoleType.USER)
                .orElseThrow(() -> new EntityNotFoundException("Role USER not found"));

        UserRole role = UserRole.builder()
                .user(user)
                .role(userRole)
                .build();

        userRoleRepository.save(role);
    }

    @Override
    public JwtResponse signin(SigninRequest request) {

        log.debug("Attempting to authenticate user: {}", request.getLogin());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String jwt = jwtService.generateToken(userDetails);

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            log.debug("User {} authenticated successfully", userDetails.getUsername());

            return JwtResponse.builder()
                    .token(jwt)
                    .login(userDetails.getUsername())
                    .roles(roles)
                    .build();
        } catch (Exception e) {
            log.error("Authentication failed for user {}: {}", request.getLogin(), e.getMessage());
            throw e;
        }
    }

    @Override
    public JwtResponse googleOauthLoginProcess(GoogleLoginInfo googleUserInfo) {

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
            userRepository.save(user);

            String jwt = jwtService.generateToken(user);

            List<String> roles = user.getRoles().stream()
                    .map(UserRole::getRole)
                    .map(role -> "ROLE_" + role.getId().name())
                    .toList();

            return JwtResponse.builder()
                    .token(jwt)
                    .login(user.getLogin())
                    .roles(roles)
                    .build();
        } else {

            Role userRole = roleRepository.findById(Role.RoleType.USER)
                    .orElseThrow(() -> new IllegalStateException("USER role not found"));

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
            user = userRepository.save(user);

            String jwt = jwtService.generateToken(user);

            return JwtResponse.builder()
                    .token(jwt)
                    .login(user.getLogin())
                    .roles(List.of("ROLE_USER"))
                    .build();

        }
    }

    @Override
    public JwtResponse githubOauthLoginProcess(GitHubLoginInfo gitHubUserInfo) {

        log.debug("Processing GitHub OAuth login for user: {}", gitHubUserInfo.getLogin());

        Optional<User> existingUser = userRepository.findByGitHubIdWithRoles(gitHubUserInfo.getId());

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            if (user.getProvider() == Provider.LOCAL) {
                throw new IllegalStateException("User with GitHub ID " + gitHubUserInfo.getId() +
                        " was registered locally. Cannot authenticate via GitHub OAuth2.");
            }

            user.setFullName(gitHubUserInfo.getName());
            user.setProfilePictureUrl(gitHubUserInfo.getAvatarUrl());
            user.setGithubId(gitHubUserInfo.getId());
            userRepository.save(user);

            String jwt = jwtService.generateToken(user);

            List<String> roles = user.getRoles().stream()
                    .map(UserRole::getRole)
                    .map(role -> "ROLE_" + role.getId().name())
                    .toList();

            log.debug("Existing GitHub user {} updated and authenticated successfully", user.getLogin());

            return JwtResponse.builder()
                    .token(jwt)
                    .login(user.getLogin())
                    .roles(roles)
                    .build();
        } else {

            Role userRole = roleRepository.findById(Role.RoleType.USER)
                    .orElseThrow(() -> new IllegalStateException("USER role not found"));

            User user = User.builder()
                    .login(gitHubUserInfo.getLogin())
                    .email(gitHubUserInfo.getEmail())
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
            user = userRepository.save(user);

            String jwt = jwtService.generateToken(user);

            log.debug("New GitHub user {} created and authenticated successfully", user.getLogin());

            return JwtResponse.builder()
                    .token(jwt)
                    .login(user.getLogin())
                    .roles(List.of("ROLE_USER"))
                    .build();
        }

    }

}
