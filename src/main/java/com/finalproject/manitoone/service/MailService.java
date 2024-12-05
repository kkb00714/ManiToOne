package com.finalproject.manitoone.service;

import com.finalproject.manitoone.constants.IllegalActionMessages;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.repository.UserRepository;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

  private static final String senderEmail = "kkb00714@gmail.com";
  private static final Random RANDOM = new Random();

  private final UserRepository userRepository;
  private final JavaMailSender javaMailSender;
  private final PasswordEncoder passwordEncoder;

  // 이메일 인증 여부 저장
  private final Map<String, Integer> verificationMap = new ConcurrentHashMap<>();
  // 인증된 이메일 저장
  private final Map<String, Boolean> verifiedEmailMap = new ConcurrentHashMap<>();

  // 랜덤으로 숫자 생성
  public static int createNumber() {
    return RANDOM.nextInt(90000) + 100000; // (최댓값 - 최소값)  + 최소값
  }

  public MimeMessage createMail(String email, int number) {
    MimeMessage message = javaMailSender.createMimeMessage();

    try {
      message.setFrom(senderEmail);
      message.setRecipients(RecipientType.TO, email);
      message.setSubject("이메일 인증");
      String body = "";
      body += "<h3>" + "요청하신 인증 번호입니다." + "</h3>";
      body += "<h1>" + number + "</h1>";
      body += "<h3>" + "감사합니다." + "</h3>";
      message.setText(body, "UTF-8", "html");
    } catch (MessagingException e) {
      throw new IllegalArgumentException(
          IllegalActionMessages.EMAIL_VERIFICATION_CODE_MISMATCH.getMessage()
      );
    }
    return message;
  }

  public void verifyEmail(String email) {
    // 1. 중복 이메일 체크
    boolean existUserByEmail = userRepository.existsByEmail(email);
    if (existUserByEmail) {
      throw new IllegalArgumentException(
          IllegalActionMessages.EMAIL_ALREADY_IN_USE.getMessage()
      );
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
      throw new IllegalArgumentException(
          IllegalActionMessages.EMAIL_VERIFICATION_CODE_MISMATCH.getMessage()
      );
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

  // 랜덤 비밀번호 생성 메서드
  private String generateRandomPassword(int length) {
    String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
    StringBuilder password = new StringBuilder();
    SecureRandom random = new SecureRandom();

    for (int i = 0; i < length; i++) {
      int index = random.nextInt(characters.length());
      password.append(characters.charAt(index));
    }
    return password.toString();
  }

  // 비밀번호 초기화 메서드
  public void findPassword(String email, String name) {

    // 1. 이메일로 사용자 검색 및 이름 일치 여부 확인
    User user = userRepository.findByEmail(email)
        .filter(u -> u.getName().equals(name))
        .orElseThrow(() ->
            new IllegalArgumentException(IllegalActionMessages.USER_NOT_FOUND.getMessage()));

    // 2. 임시 비밀번호 생성
    String temporaryPassword = generateRandomPassword(12);

    // 3. 비밀번호 암호화 및 업데이트
    String encodedPassword = passwordEncoder.encode(temporaryPassword);
    user.setPassword(encodedPassword);
    userRepository.save(user);

    // 4. 이메일 발송
    MimeMessage message = javaMailSender.createMimeMessage();
    try {
      message.setFrom(senderEmail);
      message.setRecipients(RecipientType.TO, email);
      message.setSubject("비밀번호 초기화 안내");
      String body = "<h3>안녕하세요,</h3>";
      body += "<p>임시 비밀번호는 다음과 같습니다:</p>";
      body += "<h2>" + temporaryPassword + "</h2>";
      body += "<p>로그인 후 즉시 비밀번호를 변경하시기 바랍니다.</p>";
      message.setText(body, "UTF-8", "html");
      javaMailSender.send(message);
    } catch (MessagingException e) {
      throw new IllegalArgumentException(
          IllegalActionMessages.EMAIL_VERIFICATION_FAILED.getMessage()
      );
    }
  }
}