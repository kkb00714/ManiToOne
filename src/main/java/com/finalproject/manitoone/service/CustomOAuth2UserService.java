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

    // 사용자 정보 매핑
    String provider = userRequest.getClientRegistration().getRegistrationId(); // google
    String loginId = oAuth2User.getAttribute("sub");
    String email = oAuth2User.getAttribute("email");
    String name = oAuth2User.getAttribute("name");
    String password = UUID.randomUUID().toString();  // 사용자가 OAuth2로 로그인하면 패스워드는 랜덤으로 생성

    // 이메일로 기존 사용자 조회
    User userEntity = userRepository.findOAuth2ByEmail(email);

    if (userEntity != null) {
      saveUserInfoToSession(userEntity);
    } else {
      userEntity = User.builder()
          .email(email)
          .password(passwordEncoder.encode(password))
          .name(name)
          .nickname(name)
          .birth(LocalDate.now())  // 생일은 현재 날짜로 설정
          .provider(provider)
          .loginId(loginId)
          .build();

      userRepository.save(userEntity);
      saveUserInfoToSession(userEntity);
    }

    return new PrincipalDetails(userEntity, oAuth2User.getAttributes());
  }

  // 세션에 정보 저장
  private void saveUserInfoToSession(User user) {
    session.setAttribute("email", user.getEmail());
    session.setAttribute("name", user.getName());
    session.setAttribute("nickname", user.getNickname());
    session.setAttribute("profileImage", user.getProfileImage());
    session.setAttribute("introduce", user.getIntroduce());
  }

  // 세션에서 사용자 정보를 가져옴
  public UserLoginResponseDto getUserInfoFromSession() {
    User user = (User) session.getAttribute("user");
    if (user == null) {
      throw new IllegalArgumentException("유저를 찾을 수 없습니다.");
    }
    return new UserLoginResponseDto(user);
  }
}

