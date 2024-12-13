package com.finalproject.manitoone.service;

import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.dto.PrincipalDetails;
import com.finalproject.manitoone.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;
  private final HttpSession session;
  private final PasswordEncoder passwordEncoder;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);

    String provider = userRequest.getClientRegistration().getRegistrationId();
    String loginId = oAuth2User.getAttribute("sub");
    String email = oAuth2User.getAttribute("email");
    String name = oAuth2User.getAttribute("name");
    String password = UUID.randomUUID().toString();

    User user = userRepository.findByEmail(email)
        .map(existingUser -> updateUser(existingUser, provider, loginId))
        .orElseGet(() -> createUser(email, password, name, provider, loginId));

    saveUserInfoToSession(user);

    return new PrincipalDetails(user, oAuth2User.getAttributes());
  }

  private User updateUser(User user, String provider, String loginId) {
    if (user.getProvider() == null && user.getLoginId() == null) {
      user.setProvider(provider);
      user.setLoginId(loginId);
    }
    return userRepository.save(user);
  }

  private User createUser(String email, String password, String name, String provider,
      String loginId) {
    User newUser = User.builder()
        .email(email)
        .password(passwordEncoder.encode(password))
        .name(name)
        .nickname(name)
        .birth(LocalDate.now())
        .provider(provider)
        .loginId(loginId)
        .build();
    return userRepository.save(newUser);
  }

  private void saveUserInfoToSession(User user) {
    session.setAttribute("email", user.getEmail());
    session.setAttribute("name", user.getName());
    session.setAttribute("nickname", user.getNickname());
    session.setAttribute("profileImage", user.getProfileImage());
    session.setAttribute("introduce", user.getIntroduce());
  }
}