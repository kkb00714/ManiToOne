package com.finalproject.manitoone.controller.api;

import com.finalproject.manitoone.constants.IllegalActionMessages;
import com.finalproject.manitoone.domain.dto.AuthUpdateDto;
import com.finalproject.manitoone.domain.dto.UserLoginRequestDto;
import com.finalproject.manitoone.domain.dto.UserLoginResponseDto;
import com.finalproject.manitoone.domain.dto.UserSignUpDTO;
import com.finalproject.manitoone.service.CustomOAuth2UserService;
import com.finalproject.manitoone.service.NotificationService;
import com.finalproject.manitoone.service.UserAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
  public ResponseEntity<Object> localLogin(
      @Valid
      @RequestBody UserLoginRequestDto userLoginRequestDto,
      HttpServletRequest request
  ) {
    try {
      UserLoginResponseDto userResponse = userAuthService.localLogin(
          userLoginRequestDto.getEmail(),
          userLoginRequestDto.getPassword()
      );

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

      return ResponseEntity.ok(userResponse);
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
    if (userAuthService.isEmailExist(email)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(IllegalActionMessages.EMAIL_ALREADY_IN_USE.getMessage());
    }
    return ResponseEntity.ok("사용 가능한 이메일 입니다.");
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

}
