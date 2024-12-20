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
public class UserSignUpDTO {

  @Pattern(
      regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
      message = "유효한 이메일 주소를 입력해주세요."
  )
  @NotBlank(message = "이메일은 필수 입력값입니다.")
  private String email;

  @Pattern(
      regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$",
      message = "비밀번호는 영어, 숫자, 특수문자를 포함하여 8~16자리로 설정해야 합니다."
  )
  @NotBlank(message = "비밀번호는 필수 입력값입니다.")
  private String password;

  @NotBlank(message = "이름은 필수 입력값입니다.")
  private String name;

  @Size(min = 2, max = 10, message = "닉네임은 2~10자리로 설정해야 합니다.")
  @NotBlank(message = "닉네임은 필수 입력값입니다.")
  @Pattern(regexp = "^[a-zA-Z0-9가-힣]{2,10}$", message = "닉네임은 알파벳 대소문자와 숫자만 포함할 수 있습니다.")
  private String nickname;

  @NotNull(message = "생년월일은 필수 입력값입니다.")
  private LocalDate birth;

  public User toEntity(String encryptedPassword) {
    return User.builder()
        .email(this.email)
        .password(encryptedPassword)
        .name(this.name)
        .nickname(this.nickname)
        .birth(this.birth)
        .build();
  }
}
