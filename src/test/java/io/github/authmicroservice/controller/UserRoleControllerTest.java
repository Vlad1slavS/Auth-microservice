package io.github.authmicroservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.authmicroservice.config.BaseTestSecurityConfig;
import io.github.authmicroservice.model.dto.UserRolesRequest;
import io.github.authmicroservice.model.entity.Role;
import io.github.authmicroservice.security.config.SecurityConfig;
import io.github.authmicroservice.service.UserRoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserRolesController.class)
@Import({SecurityConfig.class, BaseTestSecurityConfig.class})
@AutoConfigureMockMvc
public class UserRoleControllerTest {

    @MockitoBean
    private UserRoleService userRoleService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveUserRoles_WithAdminRole_Success() throws Exception {

        UserRolesRequest request = new UserRolesRequest();
        request.setLogin("testuser");
        request.setRoles(Arrays.asList(Role.RoleType.USER, Role.RoleType.ADMIN));

        doNothing().when(userRoleService).saveUserRoles(any(UserRolesRequest.class));

        mockMvc.perform(put("/api/v1/user-roles/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(userRoleService, times(1)).saveUserRoles(any(UserRolesRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    void saveUserRoles_WithoutAdminRole_Forbidden() throws Exception {

        UserRolesRequest request = new UserRolesRequest();
        request.setLogin("testuser");
        request.setRoles(Arrays.asList(Role.RoleType.USER));

        mockMvc.perform(put("/api/v1/user-roles/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(userRoleService, times(0)).saveUserRoles(any(UserRolesRequest.class));
    }

    @Test
    void saveUserRoles_WithoutAuthentication_Unauthorized() throws Exception {
        UserRolesRequest request = new UserRolesRequest();
        request.setLogin("testuser");
        request.setRoles(Arrays.asList(Role.RoleType.USER));

        mockMvc.perform(put("/api/v1/user-roles/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isFound());

        verify(userRoleService, times(0)).saveUserRoles(any(UserRolesRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void saveUserRoles_InvalidRequest_ReturnsBadRequest() throws Exception {

        UserRolesRequest request = new UserRolesRequest();

        mockMvc.perform(put("/api/v1/user-roles/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userRoleService, times(0)).saveUserRoles(any(UserRolesRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserRoles_ValidLogin_UserRoles() throws Exception {
        String login = "testuser";
        List<Role.RoleType> roles = Arrays.asList(Role.RoleType.USER, Role.RoleType.SUPERUSER);

        when(userRoleService.getUserRoles(eq(login))).thenReturn(roles);

        mockMvc.perform(get("/api/v1/user-roles/{login}", login))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0]").value("USER"))
                .andExpect(jsonPath("$[1]").value("SUPERUSER"));

        verify(userRoleService, times(1)).getUserRoles(eq(login));
    }

    @Test
    void getUserRoles_WithoutAuth_Forbidden() throws Exception {
        String login = "testuser";

        mockMvc.perform(get("/api/v1/user-roles/{login}", login))
                .andExpect(status().isFound());

        verify(userRoleService, times(0)).getUserRoles(eq(login));
    }

}