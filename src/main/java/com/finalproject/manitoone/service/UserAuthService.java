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
  public void registerUser(UserSignUpDTO userSignUpDTO) {
    String email = userSignUpDTO.getEmail();

    if (!mailService.isVerified(email)) {
      throw new IllegalArgumentException(
          IllegalActionMessages.EMAIL_VERIFICATION_FAILED.getMessage()
      );
    }

    boolean existUserByNickname = userRepository.existsByNickname(
        userSignUpDTO.getNickname());
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
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException(
            IllegalActionMessages.INVALID_EMAIL_OR_PASSWORD.getMessage()));

    if (!passwordEncoder.matches(password, user.getPassword())) {
      throw new IllegalArgumentException(
          IllegalActionMessages.INVALID_EMAIL_OR_PASSWORD.getMessage());
    }

    if (user.getStatus() != 1) {
      throw new IllegalArgumentException("로그인할 수 없는 상태입니다.");
    }

    return user;
  }


  @Transactional
  public UserLoginResponseDto localLogin(String email, String password) {
    User user = validateUserCredentials(email, password);
    return new UserLoginResponseDto(user);
  }

  @Transactional
  public void deleteUser(String email, String password) {
    User user = validateUserCredentials(email, password);
    user.setStatus(3);
  }

}
