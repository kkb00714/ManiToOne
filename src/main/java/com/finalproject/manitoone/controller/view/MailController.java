package com.finalproject.manitoone.controller.view;

import com.finalproject.manitoone.service.MailService;
import java.util.Map;
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
public class MailController {

  private final MailService mailService;

  // 인증 메일 전송
  @PostMapping("/email-validate")
  public ResponseEntity<String> mailSend(@RequestBody Map<String, String> request) {
    String email = request.get("email");
    try {
      mailService.verifyEmail(email);
      return ResponseEntity.ok("인증 메일이 전송되었습니다.");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("메일 전송 실패: " + e.getMessage());
    }
  }

  // 인증번호 일치여부 확인
  @PostMapping("/email-check")
  public ResponseEntity<String> mailCheck(@RequestBody Map<String, Object> request) {
    String email = (String) request.get("email");
    int verificationCode = (Integer) request.get("verificationCode");

    try {
      mailService.verifyNumber(email, verificationCode);
      return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }

  // 비밀번호 초기화 메일 발송
  @PostMapping("/password-reset")
  public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
    String email = request.get("email");
    String name = request.get("name");

    try {
      mailService.findPassword(email, name);
      return ResponseEntity.ok("임시 비밀번호가 이메일로 전송되었습니다.");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("비밀번호 초기화 실패: " + e.getMessage());
    }
  }
}
