package com.finalproject.manitoone.config;

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

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException(IllegalActionMessages.USER_NOT_FOUND.getMessage()));

    HttpSession session = request.getSession();
    session.setAttribute("email", user.getEmail());
    session.setAttribute("name", user.getName());
    session.setAttribute("nickname", user.getNickname());
    session.setAttribute("profileImage", user.getProfileImage());
    session.setAttribute("introduce", user.getIntroduce());

    response.sendRedirect("/");
  }

}
