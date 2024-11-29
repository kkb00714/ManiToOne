package com.finalproject.manitoone.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

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

  @Column(name = "unbaned_at")
  private LocalDateTime unbanedAt;

  @Column(name = "created_at", nullable = false, columnDefinition = "timestamp default now()")
  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();
}