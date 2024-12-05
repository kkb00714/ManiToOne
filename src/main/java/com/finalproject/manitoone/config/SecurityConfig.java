package com.finalproject.manitoone.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration

public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(custom -> custom
            // .antMatchers("/admin/"**).hasRole("ADMIN") // 어드민 페이지 생성 및 롤 생성 시 활성화
            .anyRequest().permitAll()
        )
//        .formLogin(custom -> custom
//            .loginPage("/login")
//            )
        .logout(custom -> custom
            .logoutSuccessUrl("/")
            .invalidateHttpSession(true)
        )
        .csrf(AbstractHttpConfigurer::disable)
        .build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
