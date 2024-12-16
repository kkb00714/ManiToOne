package com.finalproject.manitoone.aop;

import com.finalproject.manitoone.constants.IllegalActionMessages;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private final UserRepository userRepository;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    String email = authentication.getName();

    // 데이터베이스에서 사용자 조회
    User user = userRepository.findByEmail(email)
        .orElseThrow(
            () -> new IllegalArgumentException(IllegalActionMessages.USER_NOT_FOUND.getMessage()));

    // 기존 세션 무효화
    HttpSession oldSession = request.getSession(false); // 기존 세션 가져오기
    if (oldSession != null) {
      oldSession.invalidate(); // 기존 세션 무효화
    }

    // 새로운 세션 생성
    HttpSession newSession = request.getSession(true);

    // 새로운 세션에 사용자 정보 저장
    newSession.setAttribute("email", user.getEmail());
    newSession.setAttribute("name", user.getName());
    newSession.setAttribute("nickname", user.getNickname());
    newSession.setAttribute("profileImage", user.getProfileImage());
    newSession.setAttribute("introduce", user.getIntroduce());

    // 로그인 성공 후 리다이렉트
    response.sendRedirect("/");
  }
}
