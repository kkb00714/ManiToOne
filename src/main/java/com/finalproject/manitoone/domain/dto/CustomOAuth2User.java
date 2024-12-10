package com.finalproject.manitoone.domain.dto;

import java.util.Collection;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

  private final OAuth2Response oAuth2Response; // 커스텀 OAuth2 데이터 매핑 클래스
  private final Map<String, Object> attributes; // OAuth2 공급자로부터 받은 원본 데이터
  private final Collection<? extends GrantedAuthority> authorities; // 권한 정보

  // 사용자 속성 반환 (OAuth2User 필수 메서드)
  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  // 사용자 이름 반환 (OAuth2User 필수 메서드)
  @Override
  public String getName() {
    return oAuth2Response.getName();
  }

  // 사용자 권한 반환 (Spring Security 필수 메서드)
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  // 추가 메서드: 이메일 정보 가져오기
  public String getEmail() {
    return oAuth2Response.getEmail();
  }

  // 추가 메서드: OAuth2 공급자 정보 가져오기
  public String getProvider() {
    return oAuth2Response.getProvider();
  }

  // 추가 메서드: 공급자별 사용자 고유 ID 가져오기
  public String getProviderId() {
    return oAuth2Response.getProviderId();
  }
}
