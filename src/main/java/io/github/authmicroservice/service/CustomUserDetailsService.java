package io.github.authmicroservice.service;

import io.github.authmicroservice.model.entity.User;
import io.github.authmicroservice.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Сервис для загрузки пользовательских данных по имени пользователя.
 * Реализует UserDetailsService для интеграции с Spring Security
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByLoginWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        if (!user.isEnabled()) {
            throw new UsernameNotFoundException("User is disabled: " + username);
        }
        return user;
    }

}
