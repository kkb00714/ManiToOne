package com.finalproject.manitoone.config;

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

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(custom -> custom
            // .antMatchers("/admin/"**).hasRole("ADMIN") // 어드민 페이지 생성 및 롤 생성 시 활성화
            .anyRequest().permitAll()
        )
        .oauth2Login(oauth2 -> oauth2
            .loginPage("/login")
            .userInfoEndpoint(userInfo ->
                userInfo.userService(customOAuth2UserService))
            .defaultSuccessUrl("/", true)
            .failureUrl("/login-fail")
            //
            .permitAll()
        )
        .logout(custom -> custom
            .logoutSuccessUrl("/")
            .invalidateHttpSession(true)
        )
        .csrf(AbstractHttpConfigurer::disable)
        .build();
  }
}
