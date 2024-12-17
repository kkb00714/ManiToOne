package com.finalproject.manitoone.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDto {

  @Pattern(
      regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
      message = "유효한 이메일 주소를 입력해주세요."
  )
  private String email;

  @Pattern(
      regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$",
      message = "비밀번호는 영어, 숫자, 특수문자를 포함하여 8~16자리로 설정해야 합니다."
  )
  private String password;

  @Size(min = 2, max = 10, message = "닉네임은 2~10자리로 설정해야 합니다.")
  @Pattern(regexp = "^[a-zA-Z0-9가-힣]{2,10}$", message = "닉네임은 알파벳 대소문자와 숫자만 포함할 수 있습니다.")
  private String nickname;

  @Size(min = 1, max = 100)
  private String introduce;

}
