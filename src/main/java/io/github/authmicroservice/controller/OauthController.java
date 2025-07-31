package io.github.authmicroservice.controller;

import io.github.authmicroservice.model.dto.JwtResponse;
import io.github.authmicroservice.model.dto.SigninRequest;
import io.github.authmicroservice.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class OauthController {

    private final AuthService authService;

    public OauthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "message", required = false) String message,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", true);
            model.addAttribute("errorMessage", message != null ? message : "Authentication failed");
        }
        model.addAttribute("signinRequest", new SigninRequest());
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute SigninRequest signinRequest, Model model) {
        try {
            JwtResponse jwtResponse = authService.signin(signinRequest);
            return "redirect:/oauth2/redirect?token=" + jwtResponse.getToken();
        } catch (Exception e) {
            model.addAttribute("error", true);
            model.addAttribute("errorMessage", "Invalid credentials");
            model.addAttribute("signinRequest", signinRequest);
            return "login";
        }
    }

    @GetMapping("/oauth2/redirect")
    public String oauth2Redirect(@RequestParam("token") String token, Model model) {
        model.addAttribute("accessToken", token);
        return "oauth2-redirect";
    }

}

