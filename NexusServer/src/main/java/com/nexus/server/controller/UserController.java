package com.nexus.server.controller;

import com.nexus.server.dto.UserDto;
import com.nexus.server.model.User;
import com.nexus.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getProfile(
            @AuthenticationPrincipal OAuth2AuthenticationToken authentication) {

        String email = authentication.getPrincipal().getAttribute("email");
        String googleId = authentication.getPrincipal().getAttribute("sub");

        Optional<User> userOpt = userRepository.findByGoogleId(googleId)
            .or(() -> userRepository.findByEmail(email));

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        User user = userOpt.get();

        UserDto userDto = UserDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .avatarUrl(user.getAvatarUrl())
            .balance(user.getBalance())
            .build();

        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/balance")
    public ResponseEntity<Map<String, Double>> getBalance(
            @AuthenticationPrincipal OAuth2AuthenticationToken authentication) {

        String email = authentication.getPrincipal().getAttribute("email");
        String googleId = authentication.getPrincipal().getAttribute("sub");

        Optional<User> userOpt = userRepository.findByGoogleId(googleId)
            .or(() -> userRepository.findByEmail(email));

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("balance", 0.0));
        }

        return ResponseEntity.ok(Map.of("balance", userOpt.get().getBalance()));
    }
}
