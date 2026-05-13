package com.nexus.server.service;

import com.nexus.server.model.User;
import com.nexus.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String googleId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String avatarUrl = oAuth2User.getAttribute("picture");

        User user = userRepository.findByGoogleId(googleId)
            .orElseGet(() -> {
                if (userRepository.existsByEmail(email)) {
                    userRepository.findByEmail(email).ifPresent(existingUser -> {
                        existingUser.setGoogleId(googleId);
                        existingUser.setName(name);
                        existingUser.setAvatarUrl(avatarUrl);
                        userRepository.save(existingUser);
                    });
                    return userRepository.findByEmail(email).get();
                }

                User newUser = User.builder()
                    .googleId(googleId)
                    .email(email)
                    .name(name)
                    .avatarUrl(avatarUrl)
                    .balance(0.0)
                    .enabled(true)
                    .build();

                return userRepository.save(newUser);
            });

        if (!user.getEnabled()) {
            throw new OAuth2AuthenticationException("User account is disabled");
        }

        if (!user.getEmail().equals(email)) {
            user.setEmail(email);
            user.setName(name);
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);
        }

        return oAuth2User;
    }
}
