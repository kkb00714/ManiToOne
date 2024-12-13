package com.finalproject.manitoone.service;

import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.dto.PrincipalDetails;
import com.finalproject.manitoone.domain.dto.UserLoginResponseDto;
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
    // 기본 OAuth2 사용자 정보 로드
    OAuth2User oAuth2User = super.loadUser(userRequest);

    // 사용자 정보 추출
    String provider = userRequest.getClientRegistration().getRegistrationId(); // google
    String loginId = oAuth2User.getAttribute("sub");
    String email = oAuth2User.getAttribute("email");
    String name = oAuth2User.getAttribute("name");
    String password = UUID.randomUUID().toString();  // 사용자가 OAuth2로 로그인하면 패스워드는 랜덤으로 생성

    // 이메일로 기존 사용자 조회
    User userEntity = userRepository.findOAuth2ByEmail(email);
    if (userEntity == null) {
      userEntity = createUser(email, password, name, provider, loginId);
    } else {
      // 기존 사용자라면 로그인한 사용자 정보로 업데이트
      userEntity.setName(name);
      userEntity.setNickname(name);
      userEntity.setProvider(provider);
      userEntity.setLoginId(loginId);
      userRepository.save(userEntity);
    }

    // 세션에 사용자 정보 저장 (각각 따로 저장)
    saveUserInfoToSession(userEntity);

    return new PrincipalDetails(userEntity, oAuth2User.getAttributes());
  }

  // 세션에 정보 저장 (각각 따로 저장)
  private void saveUserInfoToSession(User user) {
    session.setAttribute("email", user.getEmail());
    session.setAttribute("name", user.getName());
    session.setAttribute("nickname", user.getNickname());
    session.setAttribute("profileImage", user.getProfileImage());
    session.setAttribute("introduce", user.getIntroduce());
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
    userRepository.save(newUser);
    return newUser;
  }

  // 세션에서 사용자 정보를 가져옴
  public UserLoginResponseDto getUserInfoFromSession() {
    String email = (String) session.getAttribute("email");
    String name = (String) session.getAttribute("name");
    String nickname = (String) session.getAttribute("nickname");
    String profileImage = (String) session.getAttribute("profileImage");
    String introduce = (String) session.getAttribute("introduce");

    if (email == null || name == null || nickname == null || profileImage == null || introduce == null) {
      throw new IllegalArgumentException("유저 정보를 찾을 수 없습니다.");
    }

    return UserLoginResponseDto.builder()
        .email(email)
        .name(name)
        .nickname(nickname)
        .profileImage(profileImage)
        .introduce(introduce)
        .build();
  }
}