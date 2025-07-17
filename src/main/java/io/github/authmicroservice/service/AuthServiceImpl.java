package io.github.authmicroservice.service;

import io.github.authmicroservice.model.dto.JwtResponse;
import io.github.authmicroservice.model.dto.SigninRequest;
import io.github.authmicroservice.model.dto.SignupRequest;
import io.github.authmicroservice.model.entity.Role;
import io.github.authmicroservice.model.entity.User;
import io.github.authmicroservice.model.entity.UserRole;
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

import java.util.List;
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

        log.info("Attempting to register user: {}", request.getLogin());

        if (userRepository.existsById(request.getLogin())) {
            throw new EntityExistsException("User with login " + request.getLogin() + " already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EntityExistsException("User with email " + request.getEmail() + " already exists");
        }

        User user = User.builder()
                .login(request.getLogin())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword(), request.getLogin(), request.getEmail()))
                .active(true)
                .build();

        log.info("Save user: {}", user.getLogin());

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

        log.info("Attempting to authenticate user: {}", request.getLogin());

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

            log.info("User {} authenticated successfully", userDetails.getUsername());

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

}
