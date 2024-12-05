package com.finalproject.manitoone.controller.api;

import com.finalproject.manitoone.domain.dto.UserLoginRequestDto;
import com.finalproject.manitoone.domain.dto.UserLoginResponseDto;
import com.finalproject.manitoone.domain.dto.UserSignUpDTO;
import com.finalproject.manitoone.service.UserAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
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
  public ResponseEntity<?> signIn(
      @RequestBody UserLoginRequestDto userLoginRequestDto,
      HttpSession session
  ) {
    try {
      UserLoginResponseDto responseDto = userAuthService.localLogin(
          userLoginRequestDto.getEmail(),
          userLoginRequestDto.getPassword()
      );
      session.setAttribute("user", responseDto);
      return ResponseEntity.ok(responseDto);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @GetMapping("/logout")
  public String logout(HttpServletRequest request, HttpServletResponse response) {
    new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());

    HttpSession session = request.getSession(false); // 현재 세션 가져오기
    if (session != null) {
      session.removeAttribute("user");
    }

    return "redirect:/login";
  }

  // 테스트용 - 현재 로그인된 유저 정보 반환
  @GetMapping("/current-user")
  public ResponseEntity<?> getSessionInfo(HttpSession session) {
    UserLoginResponseDto loginUser = (UserLoginResponseDto) session.getAttribute("user");
    if (loginUser == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인된 유저 정보가 없습니다.");
    }
    return ResponseEntity.ok(loginUser);
  }
}