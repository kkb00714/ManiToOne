package com.finalproject.manitoone.controller.api;

import com.finalproject.manitoone.constants.IllegalActionMessages;
import com.finalproject.manitoone.domain.dto.UserLoginRequestDto;
import com.finalproject.manitoone.domain.dto.UserLoginResponseDto;
import com.finalproject.manitoone.domain.dto.UserSignUpDTO;
import com.finalproject.manitoone.service.UserAuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserAuthController {

  private final UserAuthService userAuthService;

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
  public ResponseEntity<Object> signIn(
      @RequestBody UserLoginRequestDto userLoginRequestDto,
      HttpSession session
  ) {
    try {
      UserLoginResponseDto responseDto = userAuthService.localLogin(
          userLoginRequestDto.getEmail(),
          userLoginRequestDto.getPassword()
      );

      session.setAttribute("email", responseDto.getEmail());
      session.setAttribute("name", responseDto.getName());
      session.setAttribute("nickname", responseDto.getNickname());
      session.setAttribute("profileImage", responseDto.getProfileImage());
      session.setAttribute("introduce", responseDto.getIntroduce());

      return ResponseEntity.ok(responseDto);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest()
          .body(new IllegalArgumentException(IllegalActionMessages.USER_NOT_FOUND.getMessage()));
    }
  }

}