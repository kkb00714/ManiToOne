package com.finalproject.manitoone.domain.dto;

import com.finalproject.manitoone.domain.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUpdateDto {

  @Pattern(
      regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$",
      message = "비밀번호는 영어, 숫자, 특수문자를 포함하여 8~16자리로 설정해야 합니다."
  )
  @NotBlank(message = "비밀번호는 필수 입력값입니다.")
  private String password;

  @Size(min = 2, max = 10, message = "닉네임은 2~10자리로 설정해야 합니다.")
  @NotBlank(message = "닉네임은 필수 입력값입니다.")
  private String nickname;

  @NotNull(message = "생년월일은 필수 입력값입니다.")
  private LocalDate birth;

  // 비밀번호만 암호화 처리 후 User 엔티티로 변환
  public User toEntity(String encryptedPassword) {
    return User.builder()
        .password(encryptedPassword)
        .nickname(this.nickname)
        .birth(this.birth)
        .build();
  }
}
