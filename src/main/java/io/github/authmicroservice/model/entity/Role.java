package io.github.authmicroservice.model.entity;

import lombok.AllArgsConstructor;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Сущность роли пользователя
 */
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @Enumerated(EnumType.STRING)
    private RoleType id;

    @Column(nullable = false)
    private String name;

    public enum RoleType {
        USER,
        CREDIT_USER,
        OVERDRAFT_USER,
        DEAL_SUPERUSER,
        CONTRACTOR_RUS,
        CONTRACTOR_SUPERUSER,
        SUPERUSER,
        ADMIN
    }

}

