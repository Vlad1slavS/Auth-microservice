package io.github.authmicroservice.security.encoder;

import io.github.authmicroservice.util.SaltGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * PasswordEncoder, использует SHA-256 для хеширования паролей с уникальной солью (SaltGenerator)
 */
@Service
public class CustomPasswordEncoder implements PasswordEncoder {

    public String encode(String password, String login, String email) {
        try {
            String salt = SaltGenerator.generateUniqueSalt(login, email);
            String passwordWithSalt = password + salt;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(passwordWithSalt.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not available", e);
        }
    }

    public boolean matches(String rawPassword, String encodedPassword, String login, String email) {
        String encodedRawPassword = encode(rawPassword, login, email);
        return encodedRawPassword.equals(encodedPassword);
    }

    @Override
    public String encode(CharSequence rawPassword) {
        throw new UnsupportedOperationException("Use encode method with login and email parameters");
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        throw new UnsupportedOperationException("Use matches method with login and email parameters");
    }

}

