package com.finalproject.manitoone.controller.api;

import com.finalproject.manitoone.domain.dto.UserLoginRequestDto;
import com.finalproject.manitoone.domain.dto.UserLoginResponseDto;
import com.finalproject.manitoone.domain.dto.UserSignUpDTO;
import com.finalproject.manitoone.service.CustomOAuth2UserService;
import com.finalproject.manitoone.service.NotificationService;
import com.finalproject.manitoone.service.UserAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }

  @PostMapping("/local-login")
  public ResponseEntity<Object> localLogin(
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

      // 알림
      userResponse.setRead(notificationService.hasUnreadNotifications(
          userResponse.getEmail()));

      return ResponseEntity.ok(responseDto);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest()
          .body(new IllegalArgumentException(IllegalActionMessages.USER_NOT_FOUND.getMessage()));
    }
  }

  @DeleteMapping("/cancel-account")
  public ResponseEntity<String> deleteUser(@RequestBody UserLoginRequestDto userLoginRequestDto) {
    userAuthService.deleteUser(userLoginRequestDto.getEmail(), userLoginRequestDto.getPassword());
    return ResponseEntity.ok("회원 탈퇴 처리되었습니다.");
  }

  @GetMapping("/exist-email-and-nick")
  public ResponseEntity<Map<String, Boolean>> checkValue(
      @RequestParam String type,
      @RequestParam String value
  ) {
    boolean isTaken = userAuthService.isValueExist(type, value);
    Map<String, Boolean> response = Map.of("isTaken", isTaken);
    return ResponseEntity.ok(response);
  }

}
