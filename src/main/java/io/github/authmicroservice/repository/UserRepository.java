package io.github.authmicroservice.repository;

import io.github.authmicroservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с сущностью пользователя
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    boolean existsByEmail(String email);

    Optional<User> findByLogin(String login);

    @Query("""
    SELECT u FROM User u
    LEFT JOIN FETCH u.roles ur
    LEFT JOIN FETCH ur.role
    WHERE u.login = :login
    """)
    Optional<User> findByLoginWithRoles(String login);

}
