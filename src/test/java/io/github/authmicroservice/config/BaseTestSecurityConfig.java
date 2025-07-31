package io.github.authmicroservice.config;

import io.github.authmicroservice.security.auth.CustomAuthenticationProvider;
import io.github.authmicroservice.security.handler.OAuth2AuthenticationFailureHandler;
import io.github.authmicroservice.security.handler.OAuth2AuthenticationSuccessHandler;
import io.github.authmicroservice.service.JwtService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;

@TestConfiguration
public class BaseTestSecurityConfig {

    @Bean
    @Primary
    public JwtService jwtService() {
        return Mockito.mock(JwtService.class);
    }

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        return Mockito.mock(UserDetailsService.class);
    }

    @Bean
    @Primary
    public OAuth2AuthenticationFailureHandler oauth2AuthenticationFailureHandler() {
        return Mockito.mock(OAuth2AuthenticationFailureHandler.class);
    }

    @Bean
    @Primary
    public OAuth2AuthenticationSuccessHandler oauth2AuthenticationSuccessHandler() {
        return Mockito.mock(OAuth2AuthenticationSuccessHandler.class);
    }

    @Bean
    @Primary
    public CustomAuthenticationProvider customAuthenticationProvider() {
        return Mockito.mock(CustomAuthenticationProvider.class);
    }
}
