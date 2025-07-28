package io.github.authmicroservice.repository;

import io.github.authmicroservice.model.entity.User;
import io.github.authmicroservice.model.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с сущностью пользователя
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    boolean existsByEmailAndProvider(String email, Provider provider);

    Optional<User> findByLogin(String login);

    @Query("""
    SELECT u FROM User u
    LEFT JOIN FETCH u.roles ur
    LEFT JOIN FETCH ur.role
    WHERE u.email = :email
    AND u.provider = 'GOOGLE'
    """)
    Optional<User> findByEmailWithRoles(String email);

    @Query("""
    SELECT u FROM User u
    LEFT JOIN FETCH u.roles ur
    LEFT JOIN FETCH ur.role
    WHERE u.githubId = :githubId
    AND u.provider = 'GITHUB'
    """)
    Optional<User> findByGitHubIdWithRoles(String githubId);

    @Query("""
    SELECT u FROM User u
    LEFT JOIN FETCH u.roles ur
    LEFT JOIN FETCH ur.role
    WHERE u.login = :login
    """)
    Optional<User> findByLoginWithRoles(String login);

}
