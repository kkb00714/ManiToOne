package com.finalproject.manitoone.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User implements OAuth2User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id")
  private Long userId;

  @Column(name = "email", nullable = false, length = 50, unique = true)
  private String email;

  @Column(name = "password", nullable = false, length = 200)
  private String password;

  @Column(name = "name", nullable = false, length = 10)
  private String name;

  @Column(name = "nickname", nullable = false, length = 10, unique = true)
  private String nickname;

  @Column(name = "birth", nullable = false)
  private LocalDate birth;

  @Column(name = "introduce", nullable = false, length = 100, columnDefinition = "default 기본소개를 입력해주세요.")
  @Builder.Default
  private String introduce = "기본소개를 입력해주세요.";

  @Column(name = "profile_image", nullable = false, columnDefinition = "text default /img/defaultProfile.png")
  @Builder.Default
  private String profileImage = "/img/defaultProfile.png";

  @Column(name = "status", nullable = false, columnDefinition = "default 1 comment '1. 활동\n2. 정지\n3. 탈퇴'")
  @Builder.Default
  private Integer status = 1;

  @Column(name = "is_manito_received", nullable = false, columnDefinition = "tinyint default 1 comment '0. 수신거부\n1. 수신'")
  @Builder.Default
  private Boolean isManitoReceived = true;

  @Column(name = "role", nullable = false, length = 50, columnDefinition = "varchar(50) default 'ROLE_USER'")
  @Builder.Default
  private String role = "ROLE_USER";

  @Column(name = "unbanned_at")
  private LocalDateTime unbannedAt;

  @Column(name = "created_at", nullable = false, columnDefinition = "timestamp default now()")
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(name = "login_id", nullable = true, columnDefinition = "oauth id")
  private String loginId;

  @Column(name = "provider", nullable = false, columnDefinition = "google, local, ..., ")
  @Builder.Default
  private String provider = "Local";

  @Transient
  private Map<String, Object> attributes;

  public void setPassword(String password) {
    this.password = password;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public void setBirth(LocalDate birth) {
    this.birth = birth;
  }

  public void updateStatus(Integer status) {
    this.status = status;
  }

  public void setIntroduce(String introduce) {
    this.introduce = introduce;
  }

  public void resetUnbannedAt() {
    this.unbannedAt = null;
  }

  public void updateDefaultImage() {
    this.profileImage = "/img/defaultProfile.png";
  }

  public boolean isDefaultImage() {
    return Objects.equals(this.profileImage, "/img/defaultProfile.png");
  }

  public void updateProfileImage(String profileImage) {
    this.profileImage = profileImage;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public void setLoginId(String loginId) {
    this.loginId = loginId;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_USER"));
  }
}
