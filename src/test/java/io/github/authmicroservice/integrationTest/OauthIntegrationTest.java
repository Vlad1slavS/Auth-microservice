package io.github.authmicroservice.integrationTest;

import io.github.authmicroservice.model.dto.GitHubLoginInfo;
import io.github.authmicroservice.model.dto.GoogleLoginInfo;
import io.github.authmicroservice.model.dto.JwtResponse;
import io.github.authmicroservice.model.entity.Role;
import io.github.authmicroservice.model.entity.User;
import io.github.authmicroservice.model.entity.UserRole;
import io.github.authmicroservice.model.enums.Provider;
import io.github.authmicroservice.repository.RoleRepository;
import io.github.authmicroservice.repository.UserRepository;
import io.github.authmicroservice.repository.UserRoleRepository;
import io.github.authmicroservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers
@Transactional
public class OauthIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("auth_test")
            .withUsername("auth")
            .withPassword("1234");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    private Role userRole;

    @BeforeEach
    void setUp() {

        userRole = roleRepository.findById(Role.RoleType.USER)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .id(Role.RoleType.USER)
                        .name("USER")
                        .build()));
    }

    @Test
    void googleOauthLoginProcess_NewUser_Success() {
        GoogleLoginInfo googleInfo = GoogleLoginInfo.builder()
                .id("google12345")
                .email("test@gmail.com")
                .name("Test User")
                .picture("https://avatar.url")
                .build();

        JwtResponse response = authService.googleOauthLoginProcess(googleInfo);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("test", response.getLogin());
        assertEquals(List.of("ROLE_USER"), response.getRoles());

        Optional<User> savedUser = userRepository.findByEmailWithRoles(googleInfo.getEmail());
        assertTrue(savedUser.isPresent());

        User user = savedUser.get();
        assertEquals("test", user.getLogin());
        assertEquals("test@gmail.com", user.getEmail());
        assertEquals("Test User", user.getFullName());
        assertEquals("https://avatar.url", user.getProfilePictureUrl());
        assertEquals("google12345", user.getGoogleId());
        assertEquals(Provider.GOOGLE, user.getProvider());
        assertTrue(user.isActive());
        assertEquals(1, user.getRoles().size());
        assertEquals(Role.RoleType.USER, user.getRoles().get(0).getRole().getId());

    }

    @Test
    void googleOauthLoginProcess_ExistingGoogleUser_UpdatesAndReturnsToken() {

        User existingUser = User.builder()
                .login("oldlogin")
                .email("test@gmail.com")
                .provider(Provider.GOOGLE)
                .googleId("google123")
                .fullName("Old Name")
                .profilePictureUrl("https://old.avatar.url")
                .active(true)
                .build();

        existingUser = userRepository.save(existingUser);

        UserRole userRoleEntity = UserRole.builder()
                .user(existingUser)
                .role(userRole)
                .build();

        existingUser.setRoles(new ArrayList<>(List.of(userRoleEntity)));
        userRepository.save(existingUser);

        GoogleLoginInfo googleInfo = GoogleLoginInfo.builder()
                .id("google123")
                .email("test@gmail.com")
                .name("Updated Name")
                .picture("https://new.avatar.url")
                .build();

        JwtResponse response = authService.googleOauthLoginProcess(googleInfo);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("oldlogin", response.getLogin());
        assertEquals(List.of("ROLE_USER"), response.getRoles());

        Optional<User> updatedUser = userRepository.findByEmailWithRoles(googleInfo.getEmail());
        assertTrue(updatedUser.isPresent());

        User user = updatedUser.get();
        assertEquals("oldlogin", user.getLogin());
        assertEquals("Updated Name", user.getFullName());
        assertEquals("https://new.avatar.url", user.getProfilePictureUrl());
        assertEquals("google123", user.getGoogleId());
    }


    @Test
    void githubOauthLoginProcess_NewUser_Success() {
        GitHubLoginInfo githubInfo = GitHubLoginInfo.builder()
                .id("github123")
                .login("testuser")
                .email("test@example.com")
                .name("Test User")
                .avatarUrl("https://github.avatar.url")
                .build();

        JwtResponse response = authService.githubOauthLoginProcess(githubInfo);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("testuser", response.getLogin());
        assertEquals(List.of("ROLE_USER"), response.getRoles());

        Optional<User> savedUser = userRepository.findByGitHubIdWithRoles(githubInfo.getId());
        assertTrue(savedUser.isPresent());

        User user = savedUser.get();
        assertEquals("testuser", user.getLogin());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("Test User", user.getFullName());
        assertEquals("https://github.avatar.url", user.getProfilePictureUrl());
        assertEquals("github123", user.getGithubId());
        assertEquals(Provider.GITHUB, user.getProvider());
        assertTrue(user.isActive());
        assertEquals(1, user.getRoles().size());
        assertEquals(Role.RoleType.USER, user.getRoles().get(0).getRole().getId());
    }

    @Test
    void githubOauthLoginProcess_ExistingGithubUser_UpdatesAndReturnsToken() {

        User existingUser = User.builder()
                .login("oldlogin")
                .email("old@example.com")
                .provider(Provider.GITHUB)
                .githubId("github123")
                .fullName("Old Name")
                .profilePictureUrl("https://old.github.avatar.url")
                .active(true)
                .build();

        existingUser = userRepository.save(existingUser);

        UserRole userRoleEntity = UserRole.builder()
                .user(existingUser)
                .role(userRole)
                .build();

        existingUser.setRoles(new ArrayList<>(List.of(userRoleEntity)));
        userRepository.save(existingUser);

        GitHubLoginInfo githubInfo = GitHubLoginInfo.builder()
                .id("github123")
                .login("newlogin")
                .email("new@example.com")
                .name("Updated Name")
                .avatarUrl("https://new.github.avatar.url")
                .build();

        JwtResponse response = authService.githubOauthLoginProcess(githubInfo);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertEquals("oldlogin", response.getLogin());
        assertEquals(List.of("ROLE_USER"), response.getRoles());

        Optional<User> updatedUser = userRepository.findByGitHubIdWithRoles(githubInfo.getId());
        assertTrue(updatedUser.isPresent());

        User user = updatedUser.get();
        assertEquals("oldlogin", user.getLogin());
        assertEquals("Updated Name", user.getFullName());
        assertEquals("https://new.github.avatar.url", user.getProfilePictureUrl());
        assertEquals("github123", user.getGithubId());
    }


    @Test
    void googleOauthLoginProcess_UserRoleNotFound_ThrowsException() {
        roleRepository.deleteById(Role.RoleType.USER);

        GoogleLoginInfo googleInfo = GoogleLoginInfo.builder()
                .id("google123")
                .email("test@gmail.com")
                .name("Test User")
                .picture("https://avatar.url")
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> authService.googleOauthLoginProcess(googleInfo));

        assertTrue(exception.getMessage().contains("USER role not found"));
    }

    @Test
    void githubOauthLoginProcess_UserRoleNotFound_ThrowsException() {
        roleRepository.deleteById(Role.RoleType.USER);

        GitHubLoginInfo githubInfo = GitHubLoginInfo.builder()
                .id("github123")
                .login("testuser")
                .email("test@example.com")
                .name("Test User")
                .avatarUrl("https://github.avatar.url")
                .build();

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> authService.githubOauthLoginProcess(githubInfo));

        assertTrue(exception.getMessage().contains("USER role not found"));
    }
}