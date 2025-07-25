package io.github.authmicroservice.repository;

import io.github.authmicroservice.model.entity.Role;
import io.github.authmicroservice.model.entity.User;
import io.github.authmicroservice.model.entity.UserRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с таблицей пользователь –> роль
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    @EntityGraph(attributePaths = {"role"})
    List<UserRole> findByUser(User user);

    @Query("SELECT ur.role.id FROM UserRole ur WHERE ur.user.login = :login")
    List<Role.RoleType> findRoleTypesByUserLogin(String login);

}
