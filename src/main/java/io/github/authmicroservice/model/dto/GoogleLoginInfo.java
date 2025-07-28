package io.github.authmicroservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для хранения информации о пользователе Google при OAuth авторизации
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginInfo {

    private String id;

    private String email;

    private String name;

    private String picture;

}
