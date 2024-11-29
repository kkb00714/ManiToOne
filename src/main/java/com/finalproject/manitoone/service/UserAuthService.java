package com.finalproject.manitoone.service;

import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.dto.UserSignUpDTO;
import com.finalproject.manitoone.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAuthService {

  private final UserRepository userRepository;

  @Transactional
  public String registerUser(UserSignUpDTO userSignUpDTO) {
    // 이메일 중복 체크
    Optional<User> existUserByEmail = userRepository.findByEmail(userSignUpDTO.getEmail());
    if (existUserByEmail.isPresent()) {
      return "이메일이 이미 사용중입니다.";
    }

    if (!userSignUpDTO.getPassword().equals(userSignUpDTO.getConfirmPassword())) {
      return "비밀번호가 일치하지 않습니다.";
    }

    // 닉네임 중복 체크
    Optional<User> existUserByNickname = userRepository.findUserByNickname(userSignUpDTO.getNickname());
    if (existUserByNickname.isPresent()) {
      return "닉네임이 이미 사용중입니다.";
    }

    // 중복이 없으면 회원가입 진행
    User newUser = User.builder()
        .email(userSignUpDTO.getEmail())
        .password(userSignUpDTO.getPassword())
        .nickname(userSignUpDTO.getNickname())
        .name(userSignUpDTO.getName())
        .birth(userSignUpDTO.getBirth())
        .build();

    userRepository.save(newUser);
    return "회원가입이 완료됐습니다";
  }
}
