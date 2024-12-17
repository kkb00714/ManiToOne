package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.IllegalActionMessages;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.domain.dto.UserLoginResponseDto;
import com.finalproject.manitoone.domain.dto.UserSignUpDTO;
import com.finalproject.manitoone.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
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
  public void registerUser(UserSignUpDTO userSignUpDTO) {
    String email = userSignUpDTO.getEmail();

    if (!mailService.isVerified(email)) {
      throw new IllegalArgumentException(
          IllegalActionMessages.EMAIL_VERIFICATION_FAILED.getMessage()
      );
    }

    boolean existUserByNickname = userRepository.existsByNickname(userSignUpDTO.getNickname());
    if (existUserByNickname) {
      throw new IllegalArgumentException(
          IllegalActionMessages.NICKNAME_ALREADY_IN_USE.getMessage()
      );
    }

    String encryptedPassword = passwordEncoder.encode(userSignUpDTO.getPassword());
    User newUser = userSignUpDTO.toEntity(encryptedPassword);
    userRepository.save(newUser);

    mailService.removeVerifiedEmail(email);
  }

  @Transactional
  public User validateUserCredentials(String email, String password) {
    if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
      throw new IllegalArgumentException("이메일과 비밀번호는 필수 항목입니다.");
    }

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.INVALID_EMAIL_OR_PASSWORD.getMessage()
        ));

    boolean isPasswordMatch = passwordEncoder.matches(password, user.getPassword());
    if (!isPasswordMatch) {
      throw new IllegalArgumentException(
          IllegalActionMessages.INVALID_EMAIL_OR_PASSWORD.getMessage()
      );
    }
    return user;
  }

  @Transactional
  public UserLoginResponseDto localLogin(String email, String password) {
    User user = validateUserCredentials(email, password);
    if (user.getStatus() == 2) {
      if (user.getUnbannedAt() != null && user.getUnbannedAt().isBefore(LocalDateTime.now())) {
        user.updateStatus(1);
        user.resetUnbannedAt();
        userRepository.save(user);
      } else {
        throw new IllegalArgumentException(IllegalActionMessages.ACCESS_DENIED_PROHIBITED_USER.getMessage());
      }
    } else if (user.getStatus() == 3) {
      throw new IllegalArgumentException(IllegalActionMessages.ACCESS_DENIED_EXPIRED_USER.getMessage());
    }
    return new UserLoginResponseDto(user);
  }

  @Transactional
  public void deleteUser(String email, String password) {
    User user = validateUserCredentials(email, password);
    user.setStatus(3);
  }

  public boolean isEmailExist(String email) {
    return userRepository.existsByEmail(email);
  }

  public boolean isNicknameExist(String nickname) {
    return userRepository.existsByNickname(nickname);
  }

}
