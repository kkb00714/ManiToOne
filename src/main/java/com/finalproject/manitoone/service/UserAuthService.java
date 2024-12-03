package com.finalproject.manitoone.service;

import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.dto.UserSignUpDTO;
import com.finalproject.manitoone.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
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
      throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다.");
    }

    // 2. DTO 내부 검증 호출 - 비밀번호 검증
    userSignUpDTO.validatePasswords();

    // 3. 닉네임 중복 체크
    Optional<User> existUserByNickname = userRepository.findUserByNickname(
        userSignUpDTO.getNickname());
    if (existUserByNickname.isPresent()) {
      throw new IllegalArgumentException("닉네임이 이미 사용중입니다.");
    }

    // 4. 비밀번호 암호화 후 회원 저장
    String encryptedPassword = passwordEncoder.encode(userSignUpDTO.getPassword());
    User newUser = userSignUpDTO.toEntity(encryptedPassword);
    userRepository.save(newUser);

    // 5. 인증 완료 상태 제거
    mailService.removeVerifiedEmail(email);
    return "회원가입이 완료됐습니다";
  }
}
