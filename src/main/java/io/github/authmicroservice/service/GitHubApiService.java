package io.github.authmicroservice.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Optional;

/**
 * Сервис для работы с GitHub API (так как git не всегда возвращает email при oauth запросе)
 * В этом сервисе используется для получения почты пользователя GitHub
 */
@Service
@Slf4j
public class GitHubApiService {

    @Data
    public static class GitHubEmail {

        private String email;

        private boolean primary;

        private boolean verified;

        @JsonProperty("visibility")
        private String visibility;

    }

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final RestTemplate restTemplate;

    public GitHubApiService(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Получает primary email пользователя GitHub через API
     */
    public Optional<String> getPrimaryEmail(OAuth2AuthenticationToken token) {
        try {
            OAuth2AuthorizedClient client = authorizedClientService
                    .loadAuthorizedClient("github", token.getName());

            if (client == null) {
                log.warn("No authorized client found for GitHub");
                return Optional.empty();
            }

            String accessToken = client.getAccessToken().getTokenValue();
            return fetchPrimaryEmailFromApi(accessToken);

        } catch (Exception e) {
            log.error("Error fetching GitHub email: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    private Optional<String> fetchPrimaryEmailFromApi(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<GitHubEmail[]> response = restTemplate.exchange(
                    "https://api.github.com/user/emails",
                    HttpMethod.GET,
                    entity,
                    GitHubEmail[].class
            );

            GitHubEmail[] emails = response.getBody();
            if (emails != null) {
                return Arrays.stream(emails)
                        .filter(email -> email.isPrimary() && email.isVerified())
                        .findFirst()
                        .map(GitHubEmail::getEmail);
            }

        } catch (Exception e) {
            log.error("Failed to fetch emails from GitHub API: {}", e.getMessage());
        }

        return Optional.empty();
    }

}
