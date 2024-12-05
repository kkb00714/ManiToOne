package com.finalproject.manitoone.domain.dto.admin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchResponseDto {

  private Long userId;
  private String email;
  private String name;
  private String nickname;
  private LocalDate birth;
  private String introduce = "기본소개를 입력해주세요.";
  private String profileImage = "/img/defaultProfile.png";
  private Integer status = 1;
  private String role = "ROLE_USER";
  private LocalDateTime unbannedAt;
  private LocalDateTime createdAt = LocalDateTime.now();

}
