package io.github.authmicroservice.repository;

import io.github.authmicroservice.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с ролями пользователей
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Role.RoleType> {

}

