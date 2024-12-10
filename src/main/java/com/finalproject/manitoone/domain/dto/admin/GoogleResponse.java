package com.finalproject.manitoone.domain.dto.admin;

import com.finalproject.manitoone.domain.dto.OAuth2Response;
import java.util.Map;

public class GoogleResponse implements OAuth2Response {

  private final Map<String, Object> attributes; // Google OAuth2 응답 데이터

  public GoogleResponse(Map<String, Object> attributes) {
    this.attributes = attributes;
  }

  @Override
  public String getProvider() {
    return "google"; // Google 고정 값
  }

  @Override
  public String getProviderId() {
    return (String) attributes.get("sub"); // Google의 고유 사용자 ID
  }

  @Override
  public String getEmail() {
    return (String) attributes.get("email"); // 이메일
  }

  @Override
  public String getName() {
    return (String) attributes.get("name"); // 사용자 이름
  }
}