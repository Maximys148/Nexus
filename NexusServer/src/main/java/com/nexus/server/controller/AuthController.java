package com.nexus.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    @GetMapping("/google")
    public ResponseEntity<Map<String, String>> initiateGoogleLogin() {
        log.info("Initiating Google login");
        return ResponseEntity.ok(Map.of("redirect_uri", "/oauth2/authorization/google"));
    }

    @GetMapping("/success")
    public void loginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        log.info("Login successful for user: {}", authentication.getName());

        if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
            String email = oauthToken.getPrincipal().getAttribute("email");
            String name = oauthToken.getPrincipal().getAttribute("name");
            log.info("User details - Email: {}, Name: {}", email, name);
        }

        response.sendRedirect("http://localhost:3000/auth/success");
    }

    @GetMapping("/failure")
    public void loginFailure(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.error("Login failed");
        response.sendRedirect("http://localhost:3000/auth/failure");
    }

    @GetMapping("/logout")
    public ResponseEntity<Map<String, Boolean>> logout() {
        log.info("User logged out");
        return ResponseEntity.ok(Map.of("success", true));
    }
}
