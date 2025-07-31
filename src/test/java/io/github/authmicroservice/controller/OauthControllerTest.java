package io.github.authmicroservice.controller;

import io.github.authmicroservice.model.dto.JwtResponse;
import io.github.authmicroservice.model.dto.SigninRequest;
import io.github.authmicroservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OauthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private OauthController oauthController;

    private Model model;

    @BeforeEach
    void setUp() {
        model = mock(Model.class);
    }


    @Test
    void loginPage_Success() {
        String result = oauthController.loginPage(null, null, model);

        assertEquals("login", result);
        verify(model, times(1)).addAttribute(eq("signinRequest"), any(SigninRequest.class));
        verify(model, times(0)).addAttribute(eq("error"), any());
    }

    @Test
    void loginPage_WithErrorParam_ShowsError() {
        String result = oauthController.loginPage("true", null, model);

        assertEquals("login", result);
        verify(model, times(1)).addAttribute("error", true);
        verify(model, times(1)).addAttribute("errorMessage", "Authentication failed");
        verify(model, times(1)).addAttribute(eq("signinRequest"), any(SigninRequest.class));
    }

    @Test
    void login_ValidCredentials_RedirectsToOauth2() {
        SigninRequest signinRequest = new SigninRequest();
        signinRequest.setLogin("testuser");
        signinRequest.setPassword("password123");

        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");

        when(authService.signin(signinRequest)).thenReturn(jwtResponse);

        String result = oauthController.login(signinRequest, model);

        assertEquals("redirect:/oauth2/redirect?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9", result);
        verify(authService, times(1)).signin(signinRequest);
    }

    @Test
    void login_InvalidCredentials_ShowsError() {
        SigninRequest signinRequest = new SigninRequest();
        signinRequest.setLogin("testuser");
        signinRequest.setPassword("password");

        when(authService.signin(signinRequest))
                .thenThrow(new RuntimeException("Authentication failed"));

        String result = oauthController.login(signinRequest, model);

        assertEquals("login", result);
        verify(model, times(1)).addAttribute("error", true);
        verify(model, times(1)).addAttribute("errorMessage", "Invalid credentials");
        verify(model, times(1)).addAttribute("signinRequest", signinRequest);
        verify(authService, times(1)).signin(signinRequest);
    }

    @Test
    void oauth2Redirect_ValidToken_Success() {
        String testToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";

        String result = oauthController.oauth2Redirect(testToken, model);

        assertEquals("oauth2-redirect", result);
        verify(model, times(1)).addAttribute("accessToken", testToken);
    }

}
