package com.finalproject.manitoone.config;

import com.finalproject.manitoone.constants.IllegalActionMessages;
import com.finalproject.manitoone.domain.User;
import com.finalproject.manitoone.repository.UserRepository;
import com.finalproject.manitoone.service.CustomOAuth2UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomOAuth2UserService customOAuth2UserService;
  private final UserRepository userRepository;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(custom -> custom
            .requestMatchers("/admin/**").access((authentication, context) -> {
              HttpServletRequest request = context.getRequest();
              HttpSession session = request.getSession(false);

              if (session == null || session.getAttribute("email") == null) {
                return new AuthorizationDecision(false);
              }

              User user = userRepository.findByEmail((String) session.getAttribute("email"))
                  .orElseThrow(() -> new IllegalArgumentException(
                      IllegalActionMessages.USER_NOT_FOUND.getMessage()));
              return new AuthorizationDecision("ROLE_ADMIN".equals(user.getRole()));
            })
                // 정적 리소스 허용
                .requestMatchers("/static/**", "/style/**", "/script/**", "/images/**", "/js/**",
                    "/img/**").permitAll()

                // 모든 사용자 접근 허용
                .requestMatchers("/login-fail", "/access-deny", "/api/local-login",
                    "/api/email-validate", "/api/email-check", "/api/password-reset",
                    "/api/check-email", "/api/check-nickname", "/api/upload", "/api/update",
                    "/api/signup", "/oauth2/authorization/google")
                .permitAll()

                // 익명 사용자 전용 페이지 접근 제어
                .requestMatchers("/login", "/register", "/register-info", "/additional-info",
                    "/find-password", "/find-password-confirm")
                .access((authentication, context) -> {
                  HttpServletRequest request = context.getRequest();
                  HttpSession session = request.getSession(false);
                  // 세션에 롤이 없으면 익명 사용자로 접근 허용
                  return new AuthorizationDecision(session == null || session.getAttribute("role") == null);
                })

                // 인증된 사용자 페이지 접근 제어
                .anyRequest().access((authentication, context) -> {
                  HttpServletRequest request = context.getRequest();
                  HttpSession session = request.getSession(false);

                  if (session == null || session.getAttribute("role") == null) {
                    return new AuthorizationDecision(false);
                  }

                  // 세션의 role 확인
                  String role = (String) session.getAttribute("role");
                  return new AuthorizationDecision("ROLE_USER".equals(role) || "ROLE_ADMIN".equals(role));
                })
        )
        .addFilterBefore(new RedirectIfAuthenticatedFilter(),
            org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
        .exceptionHandling(exceptions -> exceptions
            .accessDeniedPage("/access-deny")  // 403 에러 발생 시 /access-deny로 리디렉션
        )
        .oauth2Login(oauth2 -> oauth2
            .loginPage("/login")
            .userInfoEndpoint(userInfo ->
                userInfo.userService(customOAuth2UserService))
            .failureHandler((request, response, exception) -> {
              String errorMessage = exception.getMessage();
              response.sendRedirect(
                  "/login-fail?error=" + URLEncoder.encode(errorMessage, "UTF-8"));
            })
            .failureUrl("/login-fail")
        )
        .logout(custom -> custom
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
        )
        .csrf(AbstractHttpConfigurer::disable)
        .build();
  }

  // 로그인된 사용자가 익명 페이지에 접근하면 "/"로 리다이렉트
  static class RedirectIfAuthenticatedFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain)
        throws ServletException, IOException {

      // 요청 URI 확인
      String requestURI = request.getRequestURI();

      // 익명 사용자만 접근 가능한 페이지
      boolean isAnonymousPage = requestURI.equals("/login") || requestURI.equals("/register")
          || requestURI.equals("/register-info") || requestURI.equals("/additional-info")
          || requestURI.equals("/find-password") || requestURI.equals("/find-password-confirm");

      HttpSession session = request.getSession(false);

      // 로그인 상태 검사
      boolean isLoggedIn = session != null && session.getAttribute("role") != null;

      // 로그인된 사용자가 익명 페이지에 접근하려 하면 "/"로 리다이렉트
      if (isAnonymousPage && isLoggedIn) {
        response.sendRedirect("/");
        return;
      }

      // 로그인되지 않은 사용자는 필터 체인 계속 실행
      if (!isAnonymousPage) {
        filterChain.doFilter(request, response); // 다른 요청들은 정상 실행
      } else {
        // 로그인 페이지에 대한 요청은 한 번만 필터링되도록 함
        if (!response.isCommitted()) {
          filterChain.doFilter(request, response);
        }
      }
    }
  }
}
