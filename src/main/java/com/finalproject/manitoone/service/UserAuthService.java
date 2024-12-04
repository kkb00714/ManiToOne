package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.IllegalActionMessages;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.dto.UserLoginResponseDto;
import com.finalproject.manitoone.domain.dto.UserSignUpDTO;
import com.finalproject.manitoone.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final MailService mailService;

  @Transactional
  public String registerUser(UserSignUpDTO userSignUpDTO) {
    String email = userSignUpDTO.getEmail();

    // 1. 이메일 인증 여부 확인
    if (!mailService.isVerified(email)) {
      throw new IllegalArgumentException(
          IllegalActionMessages.CANNOT_VERIFY_EMAIL.getMessage()
      );
    }

    // 2. 닉네임 중복 체크
    boolean existUserByNickname = userRepository.existsByNickname(
        userSignUpDTO.getNickname());
    if (existUserByNickname) {
      throw new IllegalArgumentException(
          IllegalActionMessages.CANNOT_USE_NICKNAME.getMessage()
      );
    }

    // 3. 비밀번호 암호화 후 회원 저장
    String encryptedPassword = passwordEncoder.encode(userSignUpDTO.getPassword());
    User newUser = userSignUpDTO.toEntity(encryptedPassword);
    userRepository.save(newUser);

    // 4. 인증 완료 상태 제거
    mailService.removeVerifiedEmail(email);
    return "회원가입이 완료됐습니다";
  }

  // 로그인 서비스
  @Transactional
  public UserLoginResponseDto localLogin(String email, String password) {
    // 1. 이메일로 사용자 조회
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.CANNOT_FIND_EMAIL_OR_PASSWORD.getMessage()));

    // 2. 비밀번호 검증
    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new IllegalArgumentException(
          IllegalActionMessages.CANNOT_FIND_EMAIL_OR_PASSWORD.getMessage());
    }

    // 3. 로그인 성공 시 사용자 정보 반환 (세션에 저장)
    return new UserLoginResponseDto(user);
  }
}
