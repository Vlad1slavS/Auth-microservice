package io.github.authmicroservice.util;

import lombok.experimental.UtilityClass;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Класс для генерации соли на основе логина и email пользователя.
 */
@UtilityClass
public class SaltGenerator {

    @Value("${auth.global.salt}")
    private static String globalSalt;

    /**
     * Генерирует соль на основе логина и email пользователя
     * Одинаковые login и email всегда дают одинаковую соль
     */
    public static String generateUniqueSalt(String login, String email) {
        try {
            String combined = globalSalt + ":" + login + ":" + email;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] saltBytes = digest.digest(combined.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(saltBytes).substring(0, 32);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Could not generate salt: " + e.getMessage(), e);
        }
    }

}
