package com.finalproject.manitoone.service;

import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.dto.CustomOAuth2User;
import com.finalproject.manitoone.domain.dto.UserLoginResponseDto;
import com.finalproject.manitoone.domain.dto.admin.GoogleResponse;
import com.finalproject.manitoone.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

    // 기본 OAuth2 사용자 정보 로드
    OAuth2User oAuth2User = super.loadUser(userRequest);
    log.info("Google OAuth2User attributes: {}", oAuth2User.getAttributes());

    // 사용자 정보 매핑
    Map<String, Object> attributes = oAuth2User.getAttributes();
    String provider = "google"; // 고정 값
    String providerId = (String) attributes.get("sub"); // Google의 고유 사용자 ID
    String email = (String) attributes.get("email");
    String name = (String) attributes.get("name");

    // 사용자 조회: 이메일과 이름을 조합하여 조회
    // todo 하드코딩, 잘못 들어가는 값들 고치기 => 생일, 비밀번호
    User user = userRepository.findByEmail(email)
        .orElseGet(() -> User.builder()
            .email(email)
            .password("DefaultPassword!1")
            .name(name)
            .nickname(name)
            .birth(LocalDate.now())
            .provider(provider)
            .loginId(providerId)
            .build()
        );

    // 사용자 정보 업데이트
    user.setLoginId(providerId);
    user.setProvider(provider);
    userRepository.save(user);

    log.info("User saved or updated: {}", user);

    // 인증된 사용자 정보를 반환
    return new CustomOAuth2User(
        new GoogleResponse(attributes), // GoogleResponse로 매핑
        attributes,
        oAuth2User.getAuthorities()
    );
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
    String email = (String) session.getAttribute("email");
    String name = (String) session.getAttribute("name");
    String nickname = (String) session.getAttribute("nickname");
    String profileImage = (String) session.getAttribute("profileImage");
    String introduce = (String) session.getAttribute("introduce");

    // 세션에 값이 없을 경우 처리
    if (email == null) {
      throw new IllegalArgumentException("OAuth2 인증이 완료되지 않았습니다.");
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

