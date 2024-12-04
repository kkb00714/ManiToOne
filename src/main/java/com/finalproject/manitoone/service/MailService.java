package com.finalproject.manitoone.service;

import com.finalproject.manitoone.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

  private final UserRepository userRepository;
  private final JavaMailSender javaMailSender;
  private static final String senderEmail = "kkb00714@gmail.com";

  // 이메일 인증 여부 저장
  private final Map<String, Integer> verificationMap = new ConcurrentHashMap<>();
  // 인증된 이메일 저장
  private final Map<String, Boolean> verifiedEmailMap = new ConcurrentHashMap<>();

  // 랜덤으로 숫자 생성
  public static int createNumber() {
    return (int) (Math.random() * (90000)) + 100000; // Math.random() * (최댓값-최소값+1) + 최소값
  }

  public MimeMessage createMail(String email, int number) {
    MimeMessage message = javaMailSender.createMimeMessage();

    try {
      message.setFrom(senderEmail);
      message.setRecipients(MimeMessage.RecipientType.TO, email);
      message.setSubject("이메일 인증");
      String body = "";
      body += "<h3>" + "요청하신 인증 번호입니다." + "</h3>";
      body += "<h1>" + number + "</h1>";
      body += "<h3>" + "감사합니다." + "</h3>";
      message.setText(body, "UTF-8", "html");
    } catch (MessagingException e) {
      throw new RuntimeException("이메일 인증이 완료되지 않았습니다.");
    }
    return message;
  }

  public void verifyEmail(String email) {
    // 1. 중복 이메일 체크
    boolean existUserByEmail = userRepository.existsByEmail(email);
    if (existUserByEmail) {
      throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
    }

    int number = createNumber();
    verificationMap.put(email, number); // 인증번호 저장
    MimeMessage message = createMail(email, number);
    javaMailSender.send(message);
  }

  // 인증번호 확인 및 인증 처리
  public void verifyNumber(String email, int inputNumber) {
    Integer savedNumber = verificationMap.get(email);
    if (savedNumber == null || savedNumber != inputNumber) {
      throw new IllegalArgumentException("인증번호가 올바르지 않습니다.");
    }
    // 인증 완료된 이메일 저장
    verifiedEmailMap.put(email, true);
    verificationMap.remove(email); // 인증번호 제거
  }

  // 인증 여부 확인
  public boolean isVerified(String email) {
    return verifiedEmailMap.getOrDefault(email, false);
  }

  // 인증 이메일 제거 (회원가입 후 정리)
  public void removeVerifiedEmail(String email) {
    verifiedEmailMap.remove(email);
  }
}
