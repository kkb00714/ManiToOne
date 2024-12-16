package com.finalproject.manitoone.config;

import com.finalproject.manitoone.aop.CustomAuthenticationSuccessHandler;
import com.finalproject.manitoone.service.CustomOAuth2UserService;
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
  private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(custom -> custom
                // .antMatchers("/admin/"**).hasRole("ADMIN") // 어드민 페이지 생성 및 롤 생성 시 활성화
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
