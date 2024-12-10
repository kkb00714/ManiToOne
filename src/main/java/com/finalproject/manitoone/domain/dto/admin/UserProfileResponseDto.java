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
public class UserProfileResponseDto {

  private Long userId;
  private String email;
  private String name;
  private String nickname;
  private LocalDate birth;
  private String introduce;
  private String profileImage;
  private Integer status;
  private String role;
  private LocalDateTime unbannedAt;
  private LocalDateTime createdAt;

}
