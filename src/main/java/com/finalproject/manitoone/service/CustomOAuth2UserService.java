package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.IllegalActionMessages;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.dto.AuthUpdateDto;
import com.finalproject.manitoone.domain.dto.PrincipalDetails;
import com.finalproject.manitoone.domain.dto.UserSignUpDTO;
import com.finalproject.manitoone.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
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

    // todo : 이미 이메일이 존재하는 유저 == 로그인을 1회 이상 한 사람이기 때문에 isNewUser를 False 처리
    boolean isNewUser = userRepository.findByEmail(email).isEmpty();

    User user = userRepository.findByEmail(email)
        .map(existingUser -> updateUser(existingUser, provider, loginId))
        .orElseGet(() -> createUser(email, password, name, provider, loginId));

    saveUserInfoToSession(user, isNewUser);

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

  private void saveUserInfoToSession(User user, boolean isNewUser) {
    session.setAttribute("email", user.getEmail());
    session.setAttribute("name", user.getName());
    session.setAttribute("nickname", user.getNickname());
    session.setAttribute("profileImage", user.getProfileImage());
    session.setAttribute("introduce", user.getIntroduce());
    session.setAttribute("isNewUser", isNewUser); // 최초 가입 여부 저장
  }

  @Transactional
  public void updateAdditionalInfo(String email, AuthUpdateDto authUpdateDto, HttpSession session) {
    // 세션 확인
    if (email == null) {
      throw new IllegalArgumentException("세션 정보가 만료되었습니다.");
    }

    // User 객체 가져와서 업데이트
    User user = userRepository.findByEmail(email)
        .orElseThrow(
            () -> new IllegalArgumentException(IllegalActionMessages.USER_NOT_FOUND.getMessage()));

    if (authUpdateDto.getPassword() != null && !authUpdateDto.getPassword().isEmpty()) {
      String encryptedPassword = passwordEncoder.encode(authUpdateDto.getPassword());
      user.setPassword(encryptedPassword); // 비밀번호 설정
    }

    // 닉네임, 생년월일 업데이트
    if (authUpdateDto.getNickname() != null) {
      user.setNickname(authUpdateDto.getNickname());
    }
    if (authUpdateDto.getBirth() != null) {
      user.setBirth(authUpdateDto.getBirth());
    }

    // 업데이트된 User 객체 저장
    userRepository.save(user);

    saveUserInfoToSession(user, false);
  }
}