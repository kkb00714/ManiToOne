package com.finalproject.manitoone.config;

import com.finalproject.manitoone.service.CustomOAuth2UserService;
import java.net.URLEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomOAuth2UserService customOAuth2UserService;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(custom -> custom
                // .antMatchers("/admin/"**).hasRole("ADMIN") // 어드민 페이지 생성 및 롤 생성 시 활성화
                .requestMatchers("/login-fail", "/access-deny", "/api/local-login",
                    "/api/email-validate", "/api/email-check", "/api/password-reset",
                    "/api/check-email", "/api/check-nickname", "/api/upload", "/api/update")
                .permitAll()
                .requestMatchers("/register", "/register-info", "/login", "/additional-info",
                    "/find-password", "/find-password-confirm", "/oauth2/authorization/google")
                .anonymous()  // 익명 사용자만 접근 가능
                .requestMatchers("/", "/style/**", "/script/**", "/js/**", "/images/**",
                    "/img/**")  // 정적 리소스
                .permitAll()
                .anyRequest().authenticated()
            // .anyRequest().permitAll()
        )
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
}
