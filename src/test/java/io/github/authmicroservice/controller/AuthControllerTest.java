package io.github.authmicroservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.authmicroservice.exception.GlobalExceptionHandler;
import io.github.authmicroservice.model.dto.JwtResponse;
import io.github.authmicroservice.model.dto.SigninRequest;
import io.github.authmicroservice.model.dto.SignupRequest;
import io.github.authmicroservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void signup_ValidRequest_Success() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setLogin("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");
        doNothing().when(authService).signup(any(SignupRequest.class));

        mockMvc.perform(put("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        verify(authService, times(1)).signup(any(SignupRequest.class));
    }

    @Test
    void signup_InvalidRequest_BadRequest() throws Exception {
        SignupRequest signupRequest = new SignupRequest();

        mockMvc.perform(put("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, times(0)).signup(any(SignupRequest.class));
    }

    @Test
    void signin_ValidRequest_Success() throws Exception {
        SigninRequest signinRequest = new SigninRequest();
        signinRequest.setLogin("testuser");
        signinRequest.setPassword("password123");

        JwtResponse jwtResponse = new JwtResponse();
        jwtResponse.setToken("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9");

        when(authService.signin(any(SigninRequest.class))).thenReturn(jwtResponse);

        mockMvc.perform(post("/api/v1/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signinRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"));

        verify(authService, times(1)).signin(any(SigninRequest.class));
    }

    @Test
    void signin_InvalidRequest_BadRequest() throws Exception {
        SigninRequest signinRequest = new SigninRequest();

        mockMvc.perform(post("/api/v1/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signinRequest)))
                .andExpect(status().isBadRequest());

        verify(authService, times(0)).signin(any(SigninRequest.class));
    }

    @Test
    void signin_ServiceThrowsException_InternalServerError() throws Exception {
        SigninRequest signinRequest = new SigninRequest();
        signinRequest.setLogin("testuser");
        signinRequest.setPassword("password123");

        when(authService.signin(any(SigninRequest.class)))
                .thenThrow(new RuntimeException("Authentication failed"));

        mockMvc.perform(post("/api/v1/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signinRequest)))
                .andExpect(status().isInternalServerError());

        verify(authService, times(1)).signin(any(SigninRequest.class));
    }


}
