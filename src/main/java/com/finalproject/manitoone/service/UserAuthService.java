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

  @Transactional
  public String registerUser(UserSignUpDTO userSignUpDTO) {
    // DTO 내부 검증 호출 - 비밀번호 검증
    userSignUpDTO.validatePasswords();

    // 이메일 중복 체크
    Optional<User> existUserByEmail = userRepository.findByEmail(userSignUpDTO.getEmail());
    if (existUserByEmail.isPresent()) {
      throw new IllegalArgumentException("이메일이 이미 사용중입니다.");
    }

    // 닉네임 중복 체크
    Optional<User> existUserByNickname = userRepository.findUserByNickname(
        userSignUpDTO.getNickname());
    if (existUserByNickname.isPresent()) {
      throw new IllegalArgumentException("닉네임이 이미 사용중입니다.");
    }

    // 비밀번호 암호화
    String encryptedPassword = passwordEncoder.encode(userSignUpDTO.getPassword());

    // 중복이 없으면 회원가입 진행
    User newUser = userSignUpDTO.toEntity(encryptedPassword);

    userRepository.save(newUser);
    return "회원가입이 완료됐습니다";
  }
}
