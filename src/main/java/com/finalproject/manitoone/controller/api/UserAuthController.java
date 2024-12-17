package com.finalproject.manitoone.controller.api;

import com.finalproject.manitoone.constants.IllegalActionMessages;
import com.finalproject.manitoone.domain.dto.AuthUpdateDto;
import com.finalproject.manitoone.domain.dto.UserLoginRequestDto;
import com.finalproject.manitoone.domain.dto.UserLoginResponseDto;
import com.finalproject.manitoone.domain.dto.UserSignUpDTO;
import com.finalproject.manitoone.domain.dto.UserUpdateDto;
import com.finalproject.manitoone.service.CustomOAuth2UserService;
import com.finalproject.manitoone.service.NotificationService;
import com.finalproject.manitoone.service.S3Service;
import com.finalproject.manitoone.service.UserAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserAuthController {

  private final UserAuthService userAuthService;
  private final CustomOAuth2UserService customOAuth2UserService;
  private final NotificationService notificationService;

  @PostMapping("/signup")
  public ResponseEntity<String> signUp(
      @Valid
      @RequestBody UserSignUpDTO userSignUpDTO
  ) {
    try {
      userAuthService.registerUser(userSignUpDTO);
      return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 완료됐습니다.");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PostMapping("/local-login")
  public ResponseEntity<String> localLogin(
      @Valid
      @RequestBody UserLoginRequestDto userLoginRequestDto,
      HttpServletRequest request
  ) {
    try {
      UserLoginResponseDto userResponse = userAuthService.localLogin(
          userLoginRequestDto.getEmail(),
          userLoginRequestDto.getPassword()
      );
      System.out.println("로그인 시도: " + userResponse.getEmail()); // 로그 찍기

      HttpSession session = request.getSession(true);

      session.setAttribute("email", userResponse.getEmail());
      session.setAttribute("nickname", userResponse.getNickname());
      session.setAttribute("name", userResponse.getName());
      session.setAttribute("profileImage", userResponse.getProfileImage());
      session.setAttribute("introduce", userResponse.getIntroduce());
      session.setAttribute("isNewUser", false);

      // 알림
      userResponse.setRead(notificationService.hasUnreadNotifications(
          userResponse.getEmail()));

      String loginSuccessMessage = "로그인 성공! 환영합니다, " + userResponse.getNickname() + " 님.";
      return ResponseEntity.ok(loginSuccessMessage);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @PutMapping("/additional-info")
  public ResponseEntity<String> additionalInfo(
      @Valid
      @RequestBody AuthUpdateDto authUpdateDto,
      HttpSession session
  ) {
    try {
      String email = (String) session.getAttribute("email");

      customOAuth2UserService.updateAdditionalInfo(email, authUpdateDto, session);

      return ResponseEntity.ok("추가 정보 입력이 완료되었습니다.");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @DeleteMapping("/cancel-account")
  public ResponseEntity<String> deleteUser(
      @Valid
      @RequestBody UserLoginRequestDto userLoginRequestDto
  ) {
    userAuthService.deleteUser(userLoginRequestDto.getEmail(), userLoginRequestDto.getPassword());
    return ResponseEntity.ok("회원 탈퇴 처리되었습니다.");
  }

  @GetMapping("/check-email")
  public ResponseEntity<String> checkEmail(
      @RequestParam String email
  ) {
    String message = userAuthService.isEmailExist(email);
    if (message.equals(IllegalActionMessages.EMAIL_ALREADY_IN_USE.getMessage())) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }
    return ResponseEntity.ok(message);
  }

  @GetMapping("/check-nickname")
  public ResponseEntity<String> checkNickname(
      @RequestParam String nickname
  ) {
    if (userAuthService.isNicknameExist(nickname)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(IllegalActionMessages.NICKNAME_ALREADY_IN_USE.getMessage());
    }
    return ResponseEntity.ok("사용 가능한 닉네임 입니다.");
  }

  @PutMapping("/update")
  public ResponseEntity<String> updateUser(@Valid @RequestBody UserUpdateDto updateDto, HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session == null || session.getAttribute("email") == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 세션이 만료되었습니다.");
    }

    String currentUserEmail = (String) session.getAttribute("email");

    if (!currentUserEmail.equals(updateDto.getEmail())) {
      throw new IllegalArgumentException("수정할 이메일이 현재 로그인한 사용자와 일치하지 않습니다.");
    }

    String result = userAuthService.updateUser(currentUserEmail, updateDto);
    return ResponseEntity.ok(result);
  }
}
